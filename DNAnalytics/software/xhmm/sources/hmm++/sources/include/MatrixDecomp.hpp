#ifndef __MATRIX_DECOMP_H__
#define __MATRIX_DECOMP_H__

#include "NamedVector.hpp"
#include "NamedMatrix.hpp"
#include "LAPACKvector.hpp"

#include <set>
using namespace std;

namespace HMM_PP {
	class MatrixDecomp {
	public:
		class SVDecomp;
		class EigenDecomp;

		/*
		 * Computes the SVD decomposition of matrix X:
		 * X = U * D * V'
		 *
		 * where U and V are orthogonal, V' means _V transposed_, and D is a
		 * diagonal matrix with the singular values D[i,i].  Equivalently, D = U' X V.
		 *
		 * The SVDecomp object returned contains:
		 * D: a vector containing the singular values of X, sorted in decreasing order
		 * U: a matrix whose columns contain the corresponding left singular vectors of X
		 * V: a matrix whose columns contain the corresponding right singular vectors of X
		 */
		static SVDecomp svd(DoubleMat* X, bool returnVtranspose = false, string workDir = "", bool deleteX = true);

		/*
		 * Computes the Right-Eigen decomposition of square matrix X.
		 *
		 * The EigenDecomp object returned contains:
		 * eigenValsReal: a vector containing the *real* parts of the eigenvalues of X
		 * eigenValsImaginary: a vector containing the *imaginary* parts of the corresponding eigenvalues of X
		 * eigenVectors: a matrix whose columns contain the corresponding right eigenvectors of X
		 */
		static EigenDecomp rightEigen(DoubleMat* X, string workDir = "", bool deleteX = true);

		class SVDecomp {
		public:
			SVDecomp(DoubleVec* singularVals, DoubleMat* leftSingVectors, DoubleMat* rightSingVectors);
			~SVDecomp();
			void deleteData();

			DoubleVec* D; // singularVals
			DoubleMat* U; // leftSingVectors
			DoubleMat* V; // rightSingVectors
		};

		class EigenDecomp {
		public:
			EigenDecomp(DoubleVec* eigenValsReal, DoubleVec* eigenValsImaginary, DoubleMat* eigenVectors);
			~EigenDecomp();
			void deleteData();

			DoubleVec* eigenValsReal;
			DoubleVec* eigenValsImaginary;
			DoubleMat* eigenVectors;
		};

		static void initSignalHandlers();

	private:
		static LAPACKvector<double>* matrixToLAPACKvector(DoubleMat* mat, bool deleteMat, const string& workDir, bool transpose = false);
		static DoubleMat* LAPACKvectorToMatrix(const double* matVec, ullint m, ullint n, bool transposeMatrix = false);

		static LAPACKvector<double>* allocateVector(ullint size, const string workDir = "");
		static void deleteAllocatedVector(HMM_PP::LAPACKvector<double>* vec);

		static void freeMappedMemory(int sig);
		static set<LAPACKvector<double>*> _mappedMem;
	};

}

#endif
