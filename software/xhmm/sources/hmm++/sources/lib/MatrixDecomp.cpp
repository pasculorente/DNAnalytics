#include "MatrixDecomp.hpp"
#include "Exception.hpp"
#include "VectorOnDisk.hpp"

#include <signal.h>

#include <algorithm>
#include <string>
#include <cctype>
using namespace std;

set<HMM_PP::LAPACKvector<double>*> HMM_PP::MatrixDecomp::_mappedMem;
void HMM_PP::MatrixDecomp::initSignalHandlers() {
	signal(SIGTERM, HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGKILL, HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGSEGV, HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGCHLD, HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGPIPE, HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGINT,  HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGABRT, HMM_PP::MatrixDecomp::freeMappedMemory);
	signal(SIGALRM, HMM_PP::MatrixDecomp::freeMappedMemory);
}

/*
 * NOTE:
 * As described here:
 * http://bytes.com/topic/c/answers/453169-dynamic-arrays-convert-vector-array
 *
 * NOTE: We can pass &vec[0] as a pointer to a contiguous memory block that contains the values in the array underlying std::vector vec
 */
extern "C" int dgesdd_(const char* jobz, lint* m, lint* n, double* a,
		lint* lda, double* s, double* u, lint* ldu,
		double* vt, lint* ldvt, double* work, lint* lwork,
		lint* iwork, lint* info);

// "S": the first min(M,N) columns of U and the first min(M,N) rows of V**T are returned in the arrays U and VT:
#define LAPACKsvd dgesdd_("S", &m, &n, Xvec, &m, &(*D)[0], Uvec, &m, VtransposeVec->rawData(), &m, work, &lwork, iwork, &info)

HMM_PP::MatrixDecomp::SVDecomp HMM_PP::MatrixDecomp::svd(DoubleMat* X, bool returnVtranspose, string workDir, bool deleteX) {
	ullintPair m_n = X->getDims();
	lint m = m_n.first;
	lint n = m_n.second;
	if (m <= 0 || n <= 0)
		throw new Exception("Cannot SVDecompose an empty matrix");

	bool swapped = false;
	LAPACKvector<double>* lVec = NULL;

	if (m <= n) {
		lVec = matrixToLAPACKvector(X, deleteX, workDir);
	}
	else {
		swap(m, n);
		swapped = true;

		lVec = matrixToLAPACKvector(X, deleteX, workDir, true);
	}
	double* Xvec = lVec->rawData();

	DoubleVec* D = new DoubleVec(m);
	double* Uvec = new double[m * m];
	LAPACKvector<double>* VtransposeVec = allocateVector(m * n, workDir);

	double* work = new double[1];
	lint lwork = -1;
	lint* iwork = new lint[8 * m];
	lint info = 0;

	// Determine size of workspace needed for SVD:
	LAPACKsvd;
	if (info != 0)
		throw new Exception("Error running FIRST dgesdd_");

	lwork = static_cast<lint>(work[0]);
	delete[] work;
	LAPACKvector<double>* workVec = allocateVector(lwork, workDir);
	work = workVec->rawData();

	// Perform actual SVD:
	LAPACKsvd;
	if (info != 0)
		throw new Exception("Error running SECOND dgesdd_");

	delete[] iwork;
	deleteAllocatedVector(workVec);
	deleteAllocatedVector(lVec);

	DoubleMat* U = LAPACKvectorToMatrix(Uvec, m, m);
	U->setMatrixName("U");
	delete[] Uvec;

	DoubleMat* V = LAPACKvectorToMatrix(VtransposeVec->rawData(), m, n, !returnVtranspose);
	string matName = "V";
	if (returnVtranspose)
		matName += DoubleMat::TRANSPOSE_SUFFIX;
	V->setMatrixName(matName);
	deleteAllocatedVector(VtransposeVec);

	if (swapped)
		swap(U, V);

	return SVDecomp(D, U, V);
}

HMM_PP::MatrixDecomp::SVDecomp::SVDecomp(DoubleVec* singularVals, DoubleMat* leftSingVectors, DoubleMat* rightSingVectors)
: D(singularVals), U(leftSingVectors), V(rightSingVectors) {
}

HMM_PP::MatrixDecomp::SVDecomp::~SVDecomp() {
}

void HMM_PP::MatrixDecomp::SVDecomp::deleteData() {
	if (D != NULL) {
		delete D;
		D = NULL;
	}

	if (U != NULL) {
		delete U;
		U = NULL;
	}

	if (V != NULL) {
		delete V;
		V = NULL;
	}
}

extern "C" int dgeev_(const char*, const char*, lint*,
		double*, lint*, double*, double*,
		double*, lint*, double*, lint*,
		double*, lint*, lint*);

#define LAPACKeigen \
		dgeev_("N",      /* left eigenvectors are not computed */ \
		"V",      /* right eigenvectors are computed */ \
		&n ,      /* order of matrix */ \
		Xvec,    /* input matrix */ \
		&n,       /* LDA */ \
		&(*eigenValsReal)[0], /* real parts of the computed eigenvalues */ \
		&(*eigenValsImaginary)[0], /* imaginary parts of the computed eigenvalues */ \
		&DUMMY_VL, /* left eigenvectors */ \
		&DUMMY_LDVL, /* LDVL */ \
		eigenVecsVector, /* right eigenvectors */ \
		&n, /* LDVR */ \
		work,  /* Workspace */ \
		&lwork, /* size of workspace */ \
		&info)

HMM_PP::MatrixDecomp::EigenDecomp HMM_PP::MatrixDecomp::rightEigen(DoubleMat* X, string workDir, bool deleteX) {
	ullintPair m_n = X->getDims();
	lint m = m_n.first;
	lint n = m_n.second;
	if (m != n || n <= 0)
		throw new Exception("Cannot Eigen-decompose a non-square or empty matrix");

	LAPACKvector<double>* lVec = matrixToLAPACKvector(X, deleteX, workDir);
	double* Xvec = lVec->rawData();

	DoubleVec* eigenValsReal = new DoubleVec(n, 0);
	DoubleVec* eigenValsImaginary = new DoubleVec(n, 0);

	double* eigenVecsVector = new double[n * n];

	double* work = new double[1];
	lint lwork = -1;
	lint info = 0;

	double DUMMY_VL;
	lint DUMMY_LDVL = 1;

	// Get workspace:
	LAPACKeigen;
	if (info != 0)
		throw new Exception("Error running FIRST dgeev_");

	// Assign workspace:
	lwork = static_cast<lint>(work[0]);
	delete[] work;
	work = new double[lwork];

	LAPACKeigen;
	if (info != 0)
		throw new Exception("Error running SECOND dgeev_");

	delete lVec;
	delete[] work;

	DoubleMat* eigenVecsMatrix = LAPACKvectorToMatrix(eigenVecsVector, n, n);
	delete[] eigenVecsVector;

	return EigenDecomp(eigenValsReal, eigenValsImaginary, eigenVecsMatrix);
}

HMM_PP::MatrixDecomp::EigenDecomp::EigenDecomp(DoubleVec* eigenValsReal, DoubleVec* eigenValsImaginary, DoubleMat* eigenVectors)
: eigenValsReal(eigenValsReal), eigenValsImaginary(eigenValsImaginary), eigenVectors(eigenVectors) {
}

HMM_PP::MatrixDecomp::EigenDecomp::~EigenDecomp() {
}

void HMM_PP::MatrixDecomp::EigenDecomp::deleteData() {
	if (eigenValsReal != NULL) {
		delete eigenValsReal;
		eigenValsReal = NULL;
	}

	if (eigenValsImaginary != NULL) {
		delete eigenValsImaginary;
		eigenValsImaginary = NULL;
	}

	if (eigenVectors != NULL) {
		delete eigenVectors;
		eigenVectors = NULL;
	}
}

void HMM_PP::MatrixDecomp::freeMappedMemory(int sig) {
	bool freedMappedMemory = false;
	for (set<LAPACKvector<double>*>::iterator i = _mappedMem.begin(); i != _mappedMem.end(); ++i) {
		freedMappedMemory = true;
		LAPACKvector<double>* mappedVec = *i;
		_mappedMem.erase(i);
		delete mappedVec;
	}
	_mappedMem.clear();

	// Reset the signal handler:
	signal(sig, HMM_PP::MatrixDecomp::freeMappedMemory);

	if (freedMappedMemory) {
		stringstream str;
		str << "Exiting due to caught ";

		char* sigCstr = strsignal(sig);
		if (sigCstr) {
			string sigStr = sigCstr;
			transform(sigStr.begin(), sigStr.end(), sigStr.begin(), (int (*)(int)) std::toupper);
			str << sigStr;
		}
		else
			str << sig;

		str << " signal after memory mapping";
		throw new Exception(str.str());
	}
}

HMM_PP::LAPACKvector<double>* HMM_PP::MatrixDecomp::allocateVector(ullint size, const string workDir) {
	HMM_PP::LAPACKvector<double>* retVec = NULL;

	if (workDir == "")
		retVec = new HMM_PP::StandardVector<double>(size);
	else {
		retVec = new HMM_PP::VectorOnDisk<double>(size, workDir);
		_mappedMem.insert(retVec);
	}

	return retVec;
}

void HMM_PP::MatrixDecomp::deleteAllocatedVector(HMM_PP::LAPACKvector<double>* vec) {
	_mappedMem.erase(vec);
	delete vec;
}

HMM_PP::LAPACKvector<double>* HMM_PP::MatrixDecomp::matrixToLAPACKvector(DoubleMat* mat, bool deleteMat, const string& workDir, bool transpose) {
	HMM_PP::ullintPair m_n = mat->getDims();
	ullint m = m_n.first;  // # of rows
	ullint n = m_n.second; // # of columns

	HMM_PP::LAPACKvector<double>* retVec = allocateVector(m * n);
	double* matVec = retVec->rawData();

	ullint ind = 0;
	ullint row = m-1;
	while (true) {
		for (ullint col = 0; col < n; ++col) {
			if (!transpose) // LAPACK requires conversion to vector in column-major order:
				ind = col * m + row;
			else // want to transpose, so insert data in row-major order:
				ind = row * n + col;

			matVec[ind] = (*mat)(row, col);
		}

		// Delete as we go to save space:
		if (deleteMat)
			mat->dropLastRow();

		if (row == 0)
			break;
		else
			--row;
	}

	if (deleteMat)
		delete mat;

	return retVec;
}

HMM_PP::DoubleMat* HMM_PP::MatrixDecomp::LAPACKvectorToMatrix(const double* matVec, ullint m, ullint n, bool transposeMatrix) {
	DoubleMat* mat = new DoubleMat();

	ullint k = 0;

	if (!transposeMatrix) { // use the default LAPACK column-major order
		mat->setDims(m, n);
		for (ullint col = 0; col < n; ++col)
			for (ullint row = 0; row < m; ++row)
				(*mat)(row, col) = matVec[k++];
	}
	else { // transpose the matrix, so insert in row-major order
		mat->setDims(n, m);
		for (ullint row = 0; row < n; ++row)
			for (ullint col = 0; col < m; ++col)
				(*mat)(row, col) = matVec[k++];
	}

	return mat;
}
