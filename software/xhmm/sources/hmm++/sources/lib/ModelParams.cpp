#include "ModelParams.hpp"
#include "Model.hpp"
#include "Exception.hpp"

#include <cmath>
#include <fstream>
using namespace std;

HMM_PP::ModelParams::ModelParams(HiddenStateParams* params)
: _params(params) {
}

HMM_PP::ModelParams::~ModelParams() {
	delete _params;
}

vector<double>* HMM_PP::ModelParams::emissionFunc(const HMM_PP::Data* sequence, const uint t) const {
	vector<double>* emissProbs = new vector<double>(getNumHiddenStates());

	for (uint i = 0; i < getNumHiddenStates(); ++i)
		(*emissProbs)[i] = emissionFunc(sequence, i, t);

	return emissProbs;
}

double HMM_PP::ModelParams::dnorm(const double x, const double u, const double v) {
	return (1 / sqrt(2 * M_PI * v)) * exp(- ((x-u)*(x-u) / (2 * v)));
}

double HMM_PP::ModelParams::univarNorm(const double x, const double m, const double sd) {
	double z = (x - m) / sd;
	double sqrt2pi = 2.50662827463;
	double t0, z1, p0;

	t0 = 1 / (1 + 0.2316419* fabs(z));
	z1 = exp(-0.5* z*z) / sqrt2pi;
	p0 = z1* t0
			* (0.31938153 +
					t0* (-0.356563782 +
							t0* (1.781477937 +
									t0* (-1.821255978 +
											1.330274429* t0))));
	return z >= 0 ? 1 - p0 : p0;
}

/*
 * HiddenStateParams
 */
HMM_PP::ModelParams::HiddenStateParams::HiddenStateParams(istream& stream)
: _initProbs() {

	uint numHiddenStates;
	stream >> numHiddenStates;
	if (!stream)
		throw new Exception("Failed to read number of hidden states from input stream");
	setNumHiddenStates(numHiddenStates);

	for (uint i = 0; i < getNumHiddenStates(); i++) {
		string label;
		stream >> label;
		if (!stream)
			throw new Exception("Failed to read names of hidden states from input stream");
		relabelHiddenState(i, label);
	}
}

void HMM_PP::ModelParams::HiddenStateParams::setNumHiddenStates(const uint k)  {
	_statemap.clear();
	_statermap.clear();
	for (uint i = 0; i < k; i++)
		addState("S" + int2str(i+1));
}

void HMM_PP::ModelParams::HiddenStateParams::addState(const string& label) {
	_statemap.push_back(label);
	_statermap[ label ] = _statemap.size() - 1 ; // 0-based codes
}

void HMM_PP::ModelParams::HiddenStateParams::relabelHiddenState(const uint i, const string& label) {
	// first clear old label
	_statermap.erase(_statermap.find(_statemap[i]));
	_statemap[i] = label;
	_statermap[label] = i;
}

vector<uint> HMM_PP::ModelParams::HiddenStateParams::state(const vector<string>& s) const {
	vector<uint> r(s.size());
	for (uint i = 0; i < s.size(); i++)
		r[i] = state(s[i]);
	return r;
}

vector<string> HMM_PP::ModelParams::HiddenStateParams::state(const vector<uint>& s) const {
	vector<string> r(s.size());
	for (uint i = 0; i < s.size(); i++)
		r[i] = state(s[i]);
	return r;
}

HMM_PP::DoubleVec HMM_PP::ModelParams::HiddenStateParams::getUniformStartProbs() const {
	return DoubleVec(getNumHiddenStates(), 1/(double)getNumHiddenStates());
}

void HMM_PP::ModelParams::HiddenStateParams::setStartingProbsAndNormalize(const DoubleVec& x) {
	_initProbs = x;
	_initProbs.normalize();
}
