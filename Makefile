JAVAC   = javac
JAVA    = java
SRC_DIR = src
BIN     = bin

# Tous les .java
SRC_ALL  := $(shell find $(SRC_DIR) -name "*.java")
# Tous les .java sauf ceux dans src/Experimentation/...
SRC_CORE := $(filter-out $(SRC_DIR)/Experimentation/%, $(SRC_ALL))
# Seulement les fichiers d'expérimentation
SRC_EXP  := $(filter $(SRC_DIR)/Experimentation/%, $(SRC_ALL))

JARS = lib/jfreechart-1.5.4.jar:lib/jcommon-1.0.24.jar

all: compile

compile:
	@echo ">> Compilation du projet (sans Experimentation)..."
	@mkdir -p $(BIN)
	@$(JAVAC) -d $(BIN) $(SRC_CORE)
	@echo ">> Compilation terminée."

compile_exp:
	@echo ">> Compilation des fichiers Experimentation..."
	@mkdir -p $(BIN)
	@$(JAVAC) -cp "$(BIN)" -d $(BIN) $(SRC_EXP)
	@echo ">> Compilation Experimentation terminée."

experiment: compile compile_exp
	@echo ">> Lancement expérimentations..."
	@$(JAVA) -cp "$(BIN)" Experimentation.ExperimentLauncher

plot: compile compile_exp
	@echo ">> Génération des courbes..."
	@$(JAVA) -cp "$(BIN):$(JARS)" Experimentation.PlotCurves

clean:
	@rm -rf $(BIN)/*
	@rm -rf data/*
	@rm -rf out/*
	@echo ">> Nettoyage terminé."
