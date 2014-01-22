HMM_PP_LIB_BASE_DIR =./sources/hmm++

include $(HMM_PP_LIB_BASE_DIR)/config_defs.Makefile

MAKE_NAME =xhmm

########################
##Xhmm command line options
########################
XHMM_CMDLINE =xhmmCmdline

XHMM_CMDLINE_OPTIONS_FILE =$(INC_DIR)/$(XHMM_CMDLINE).ggo

XHMM_CMDLINE_HEADER =$(INC_DIR)/$(XHMM_CMDLINE).h
XHMM_CMDLINE_SOURCE =$(LIB_SRC_DIR)/$(XHMM_CMDLINE).c

XHMM_CMDLINE_OUTPUT_FILES =$(XHMM_CMDLINE_HEADER) $(XHMM_CMDLINE_SOURCE)


#Header file needed to be made:
DEPEND_HEADERS += $(XHMM_CMDLINE_HEADER)

##############################################
# command-line option parsing
##############################################
GENGETOPT =$(shell which gengetopt 2> /dev/null || echo NOT_FOUND)


#######################
#Source files
#######################

#xhmm executable [main() function] files:
EXECS_SUB_DIR =execs
EXECS_SRC_DIR =$(SRC_DIR)/$(EXECS_SUB_DIR)
EXECS_SRC =$(wildcard $(EXECS_SRC_DIR)/*.cpp)

TARGETS =$(subst $(SRC_DIR),$(BLD_DIR),$(EXECS_SRC:.cpp=))
TARGETS_NO_DIR =$(notdir $(TARGETS))
TARGETS_SOFT_LINKS =$(addprefix ./, $(TARGETS_NO_DIR))

BLD_LIB_DIR =$(BLD_DIR)/$(LIB_SUB_DIR)
BLD_EXECS_DIR =$(BLD_DIR)/$(EXECS_SUB_DIR)

ALL_BLD_DIRS += $(BLD_EXECS_DIR)

SRC_DEPENDS += $(EXECS_SRC)
# Remove xhmmCmdline.c from the list of source files to create dependencies [to prevent gengetopt being run twice]:
SRC_DEPENDS := $(subst $(XHMM_CMDLINE_SOURCE),,$(SRC_DEPENDS))

REMOVES += $(TARGETS_SOFT_LINKS)

ifneq ($(GENGETOPT), NOT_FOUND)
REMOVES += $(XHMM_CMDLINE_OUTPUT_FILES)
endif

# Files to be included when making the 'zip' file:
ZIP_INCLUDES =README params.txt

########################
#Static Libraries
########################

# xhmm library:
XHMM_LIB_MAKE_DIR =./
XHMM_LIB_DIR =$(BLD_LIB_DIR)/
XHMM_LIBS =lib$(MAKE_NAME).a
XHMM_INC =$(INC_DIR)/
XHMM_LIB_FULL_PATH =$(XHMM_LIB_DIR)$(XHMM_LIBS)

# hmm++ library:
HMM_PP_LIB_MAKE_DIR =$(HMM_PP_LIB_BASE_DIR)/
HMM_PP_LIB_DIR =$(HMM_PP_LIB_BASE_DIR)/$(BLD_LIB_DIR)/
HMM_PP_LIBS =libhmm++.a
HMM_PP_INC =$(HMM_PP_LIB_BASE_DIR)/$(INC_DIR)/
HMM_PP_LIB_FULL_PATH =$(HMM_PP_LIB_DIR)$(HMM_PP_LIBS)


#For example: -I./
LIB_INCLUDE_DIRS += $(XHMM_INC) $(HMM_PP_INC)

#For example: ./sources/hmm++/build/lib/libhmm++.a
LIB_DEPENDS =$(XHMM_LIB_FULL_PATH) $(HMM_PP_LIB_FULL_PATH)
LIB_DEPENDS_NO_PATH =$(notdir $(LIB_DEPENDS))

ifeq ($(LAPACK_LIBS),)
#LAPACK_LIBS=lapack f77blas cblas atlas gfortran
#LAPACK_LIBS=acml gfortran
LAPACK_LIBS=lapack
endif
LAPACK_LIB_LINKS =$(LAPACK_LIBS:%=-l%)

#For example: -lhmm++
LIB_LINKS =$(LIB_DEPENDS_NO_PATH:lib%.a=-l%) -lpthread $(LAPACK_LIB_LINKS)

#For example: -L./
LIB_LINK_DIRS =$(XHMM_LIB_DIR) $(HMM_PP_LIB_DIR)
LIB_LINK_FLAGS =$(LIB_LINK_DIRS:%=-L%)

#All directories with relevant libraries' makefiles other than this one [$(XHMM_LIB_MAKE_DIR)]; for example: ./sources/hmm++
LIB_MAKE_DIRS =$(HMM_PP_LIB_MAKE_DIR)


##########################################
#Compiler, Compilation flags:
##########################################

ALL_CXXFLAGS_LINKER =$(CXXFLAGS) $(LIB_LINK_FLAGS)

USE_DBGCXXFLAGS =DBGCXXFLAGS="$(DBGCXXFLAGS)" USE_DEBUG="$(USE_DEBUG)"

########################
#Rules
########################

#'make all' creates all dependencies, always calls make on libraries, and then on targets:
.DEFAULT_GOAL := all
.PHONY: all
all:
	$(MAKE) $(LIB_MAKE_DIRS)
	$(MAKE) $(TARGETS_SOFT_LINKS)

.PHONY: $(LIB_MAKE_DIRS)
$(LIB_MAKE_DIRS):
	$(MAKE) $(USE_DBGCXXFLAGS) -C $@


# command-line option parsing:
ifneq ($(GENGETOPT), NOT_FOUND)
$(XHMM_CMDLINE_OUTPUT_FILES): $(XHMM_CMDLINE_OPTIONS_FILE)
	gengetopt -i $< -F $(XHMM_CMDLINE) --header-output-dir=$(INC_DIR) --src-output-dir=$(LIB_SRC_DIR) --conf-parser
endif


# The soft-link pointing to each of the executables listed in $(TARGETS):
$(TARGETS_SOFT_LINKS): %: $(BLD_EXECS_DIR)/%
	ln -fs $< $@

#explicit rules for making executables:
$(TARGETS): %: %.o $(LIB_DEPENDS) $(ENSURE_BUILD_DIRS_PREREQS)
	$(CXX) $(ALL_CXXFLAGS_LINKER) $^ $(LIB_LINKS) -o $@



# Static library rules:
$(HMM_PP_LIB_FULL_PATH):
	$(MAKE) $(USE_DBGCXXFLAGS) -C $(HMM_PP_LIB_MAKE_DIR)



# Clean-up rules:
clean: $(addsuffix .clean,$(LIB_MAKE_DIRS))
.PHONY: %.clean
%.clean:
	$(MAKE) -C $* clean



include $(HMM_PP_LIB_BASE_DIR)/config_rules.Makefile
