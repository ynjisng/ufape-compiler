package codegen;

import java.util.ArrayList;
import java.util.List;

import ast.*;

public class ThreeAdressCodeGenerator {
    private List<ThreeAdressCodeInstruction> instructions;
    private int tempCounter;

    public ThreeAdressCodeGenerator() {
        this.instructions = new ArrayList<>();
        this.tempCounter = 0;
    }

    public List<ThreeAdressCodeInstruction> generate(Programa programa) {
        generateBloco(programa.getBloco());
        return instructions;
    }

    private void generateBloco(Bloco bloco) {
        // Gera código para declarações de variáveis
        generateDeclaracaoVariavel(bloco.getDeclaracaoVariavel());

        // Gera código para os comandos
        for (Comando comando : bloco.getComandos()) {
            generateComando(comando);
        }
    }

    private void generateDeclaracaoVariavel(List<DeclaracaoVariavel> declaracoes) {
        for (DeclaracaoVariavel declaracao : declaracoes) {
            String nomeVariavel = declaracao.getIdentificador();
            Expressao valorInicial = declaracao.getValorInicializado();
    
            if (!(valorInicial instanceof ExpressaoNula)) {
                // Gera código para inicializar a variável, se houver valor inicial
                String temp = generateExpressao(valorInicial);
                instructions.add(new ThreeAdressCodeInstruction("=", temp, null, nomeVariavel));
            } else {
                // Variáveis sem valor inicial são apenas declaradas
                instructions.add(new ThreeAdressCodeInstruction("DECLARE", null, null, nomeVariavel));
            }
        }
    }

    private void generateComando(Comando comando) {
        if (comando instanceof ComandoAtribuicao) {
            generateComandoAtribuicao((ComandoAtribuicao) comando);
        } else if (comando instanceof ComandoEnquanto) {
            generateComandoEnquanto((ComandoEnquanto) comando);
        } else if (comando instanceof ComandoCondicional) {
            generateComandoCondicional((ComandoCondicional) comando);
        } else if (comando instanceof ComandoChamadaProcedure) {
            generateComandoChamadaProcedure((ComandoChamadaProcedure) comando);
        }
    }

    private void generateComandoAtribuicao(ComandoAtribuicao comando) {
        String identificador = comando.getIdentificador();
        for (Expressao expressao : comando.getExpressao()) {
            String temp = generateExpressao(expressao);
            instructions.add(new ThreeAdressCodeInstruction("=", temp, null, identificador));
        }
    }

    private void generateComandoEnquanto(ComandoEnquanto comando) {
        String startLabel = "L" + tempCounter++;
        String endLabel = "L" + tempCounter++;

        instructions.add(new ThreeAdressCodeInstruction("LABEL", startLabel, null, null));
        String condition = generateExpressao(comando.getExpressao().get(0));
        instructions.add(new ThreeAdressCodeInstruction("IF_FALSE", condition, null, endLabel));

        for (Comando cmd : comando.getComando()) {
            generateComando(cmd);
        }

        instructions.add(new ThreeAdressCodeInstruction("GOTO", startLabel, null, null));
        instructions.add(new ThreeAdressCodeInstruction("LABEL", endLabel, null, null));
    }

    private void generateComandoCondicional(ComandoCondicional comando) {
        String elseLabel = "L" + tempCounter++;
        String endLabel = "L" + tempCounter++;

        String condition = generateExpressao(comando.getExpressao().get(0));
        instructions.add(new ThreeAdressCodeInstruction("IF_FALSE", condition, null, elseLabel));

        for (Comando cmd : comando.getComandosIf()) {
            generateComando(cmd);
        }

        instructions.add(new ThreeAdressCodeInstruction("GOTO", endLabel, null, null));
        instructions.add(new ThreeAdressCodeInstruction("LABEL", elseLabel, null, null));

        for (Comando cmd : comando.getComandosElse()) {
            generateComando(cmd);
        }

        instructions.add(new ThreeAdressCodeInstruction("LABEL", endLabel, null, null));
    }

    private void generateComandoChamadaProcedure(ComandoChamadaProcedure comando) {
        for (Expressao parametro : comando.getParametros()) {
            String temp = generateExpressao(parametro);
            instructions.add(new ThreeAdressCodeInstruction("PARAM", temp, null, null));
        }
        instructions.add(new ThreeAdressCodeInstruction("CALL", comando.getNome(), null, null));
    }

    private String generateExpressao(Expressao expressao) {
        if (expressao instanceof ExpressaoFatorVariavel) {
            return ((ExpressaoFatorVariavel) expressao).getIdentificador();
        } else if (expressao instanceof ExpressaoFatorAtributo) {
            return ((ExpressaoFatorAtributo) expressao).getAtributo().toString();
        } else if (expressao instanceof ExpressaoCompleta) {
            ExpressaoCompleta completa = (ExpressaoCompleta) expressao;
            String left = generateExpressao(completa.getEsquerda());
            String right = generateExpressao(completa.getDireita());
            String temp = newTemp();
            instructions.add(new ThreeAdressCodeInstruction(completa.getOperador(), left, right, temp));
            return temp;
        } else if (expressao instanceof ExpressaoFatorChamadaFunction) {
            ExpressaoFatorChamadaFunction chamadaFunction = (ExpressaoFatorChamadaFunction) expressao;
            for (Argumento argumento : chamadaFunction.getArgumentos()) {
                String temp = generateExpressao(argumento.getExpressao());
                instructions.add(new ThreeAdressCodeInstruction("PARAM", temp, null, null));
            }
            String temp = newTemp();
            instructions.add(new ThreeAdressCodeInstruction("CALL", chamadaFunction.getNome(), null, temp));
            return temp;
        }
        return null;
    }

    private String newTemp() {
        return "t" + tempCounter++;
    }
}
