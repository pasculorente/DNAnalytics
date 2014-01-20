#ifndef __DATA_LOADER_H__
#define __DATA_LOADER_H__

#include "utils.hpp"
#include "Data.hpp"
#include "Exception.hpp"
#include "utils.hpp"

#include <iostream>
#include <sstream>
#include <string>
using namespace std;

namespace HMM_PP {

	template<class DataType>
	class DataLoader {

	public:
		DataLoader(string dataFile, bool readHeaderLine = true, bool readDataIDs = true);
		virtual ~DataLoader();

		virtual bool hasNext();
		virtual DataType* next();

		const string* getHeader() const { return _header; }

	protected:
		istreamLineReader* _stream;

		string* _header;

		bool _readDataIDs;
		uint _idCount;

		virtual void setValFromStream(typename DataType::InputType& f, istream& stream);
	};

	template<class DataType>
	DataLoader<DataType>::DataLoader(string dataFile, bool readHeaderLine, bool readDataIDs)
	: _stream(utils::getIstreamLineReaderFromFile(dataFile)), _header(NULL), _readDataIDs(readDataIDs), _idCount(0) {

		if (readHeaderLine) {
			if (!hasNext())
				throw new Exception("Unable to read header line from input stream");

			_header = new string();
			*_stream >> *_header;

			if (readDataIDs) {
				stringstream* lineStream = new stringstream(*_header);

				string dummy;
				// Instead of splitting by whitespace, use ONLY tab as delimiter for the row ID ("sample name"):
				if (!getline(*lineStream, dummy, '\t') || !*lineStream)
					throw new Exception("Data input stream failed while reading Matrix title");

				// Read the remainder of the line into _header:
				stringbuf* headerBuf = new stringbuf();
				lineStream->get(*headerBuf);
				if (!*lineStream)
					throw new Exception("Unable to read header line from input stream");
				delete lineStream;
				*_header = headerBuf->str();
				delete headerBuf;
			}
		}
	}

	template<class DataType>
	DataLoader<DataType>::~DataLoader() {
		if (_stream != NULL)
			delete _stream;

		if (_header != NULL)
			delete _header;
	}

	template<class DataType>
	bool DataLoader<DataType>::hasNext() {
		return !_stream->eof();
	}

	template<class DataType>
	DataType* DataLoader<DataType>::next() {
		if (!hasNext())
			throw new Exception("Illegal call to next() when !hasNext()");

		string* line = new string();
		*_stream >> *line;
		stringstream* lineStream = new stringstream(*line);
		delete line;

		string id;
		if (_readDataIDs) {
			// Instead of splitting by whitespace, use ONLY tab as delimiter for the row ID ("sample name"):
			if (!getline(*lineStream, id, '\t') || !*lineStream)
				throw new Exception("Unable to read data ID from stream");
		}
		else {
			stringstream str;
			str << "ID" << ++_idCount;
			id = str.str();
		}

		DataType* d = new DataType();
		d->setId(id);

		while (*lineStream && !lineStream->eof()) {
			typename DataType::InputType f;
			setValFromStream(f, *lineStream);
			d->addDatapoint(f);
		}

		delete lineStream;

		return d;
	}

	template<class DataType>
	void DataLoader<DataType>::setValFromStream(typename DataType::InputType& f, istream& stream) {
		stream >> f;
		if (!stream)
			throw new Exception("Data input stream failed while reading value");
	}
}

#endif
