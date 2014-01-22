#ifndef __UNIVARIATE_QUANTITATIVE_MODEL_PARAMS_H__
#define __UNIVARIATE_QUANTITATIVE_MODEL_PARAMS_H__

#include "utils.hpp"
#include "HomogeneousModelParams.hpp"
#include "NamedMatrix.hpp"
#include "NamedVector.hpp"
#include "UnivariateQuantitativeData.hpp"
#include "ModelParamsData.hpp"

namespace HMM_PP {

	class UnivariateQuantitativeModelParams : public HomogeneousModelParams, public ModelParamsData<UnivariateQuantitativeData> {

	public:
		UnivariateQuantitativeModelParams(istream& stream, HMM_PP::DataLoader<UnivariateQuantitativeData>* dataLoader);
		virtual ~UnivariateQuantitativeModelParams() {}

	protected:
		UnivariateQuantitativeModelParams(HiddenStateParams* params, HMM_PP::DataLoader<UnivariateQuantitativeData>* dataLoader = NULL)
		: ModelParams(params),
		  HomogeneousModelParams(params),
		  HMM_PP::ModelParamsData<UnivariateQuantitativeData>(params, dataLoader),
		  _mean(), _var() {}

		void constructVariableParamsFromStream(istream& stream);

		static void readGaussianParams(istream& stream, const uint numHiddenStates, DoubleVec& paramValues);
		void getGaussianParams(DoubleVec& v) const;
		void setGaussianFromParams(const DoubleVec& v, uint& vInd);

	public:
		// Updates
		virtual DoubleVec getVariableParams() const;
		virtual void setVariableParams(const DoubleVec& v);

		// Get/calculate values
		virtual double emissionFunc(const HMM_PP::Data* sequence, const uint i, const uint t) const;
		virtual vector<double>* emissionFunc(const HMM_PP::Data* sequence, const uint t) const;

		virtual void clear();
		virtual void updateEmission(const HMM_PP::InferredDataFit*);
		virtual void scale(const double n);

	protected:
		// mean and variance for emission probs
		DoubleVec _mean;
		DoubleVec _var;

		// display model
		virtual ostream& print(ostream& stream) const;

	public:
		class QuantitativeDataVal : public DataVal {
		public:
			QuantitativeDataVal(double val = 0.0) : _val(val) {}
			virtual ~QuantitativeDataVal() {}

			virtual ostream& print(ostream& stream) const {
				stream << _val;
				return stream;
			}

		private:
			double _val;
		};

		virtual QuantitativeDataVal* calcRepresentativeDataVal(const HMM_PP::Data* d, const uint t1, const uint t2) const;
	};
}

#endif
