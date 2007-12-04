SYNTAX     = Stratego
EXTENSIONS = str
PACKAGE    = org.strategoxt.imp.editors.stratego
SOURCE     = ~/.nix-profile/share/sdf/stratego-front/Stratego.def
START      = Module

SDF2IMP    = ../org.strategoxt.imp.metatooling/sdf2imp

all : eclipse-deps

clean :
	rm -f src/parser/$(SYNTAX)ParseController.java plugin.xml
	rm -rf src/parser/ast
	# ...

eclipse-deps : plugin.xml # and others

plugin.xml : $(SYNTAX).def $(SYNTAX).tbl $(SDF2IMP) Makefile
	$(SDF2IMP) -i $< -p $(SYNTAX).tbl -e $(EXTENSIONS) -m $(SYNTAX) -s $(START) -n $(PACKAGE) -r --verbose 2

$(SYNTAX).def : $(SOURCE)
	ln -s $< $@

$(SYNTAX).tbl : $(SYNTAX).def
	sdf2table -i $< -o $@ -m $(SYNTAX)
