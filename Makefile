JARS = lib/jfreechart-1.5.4.jar:lib/jcommon-1.0.24.jar
SRC  = $(shell find src -name "*.java")
BIN  = bin

all: compile

compile:
	@echo ">> Compilation du projet..."
	@javac -cp "$(JARS)" -d $(BIN) $(SRC)
	@echo ">> Compilation terminée."

experiment:
	@echo ">> Lancement expérimentations..."
	@java -cp "$(BIN)" Experimentation.ExperimentLauncher

plot:
	@echo ">> Génération des courbes..."
	@java -cp "$(BIN):$(JARS)" Experimentation.PlotCurves

clean:
	@rm -rf bin/*
	@rm -rf data/*
	@rm -rf out/*
	@echo ">> Nettoyage terminé."
