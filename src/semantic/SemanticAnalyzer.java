package semantic;

import java.util.List;

import ast.*;
import symbol.SymbolTable;
import symbol.SymbolTableEntry;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private String currentScope = "global";
    private boolean inLoop = false;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void analyze(Programa programa) {
        // Implementar a lógica de análise semântica aqui
        analyzeBloco(programa.getBloco());
        System.out.println("Análise semântica concluída com sucesso. Nenhum erro encontrado.");
    }   

    private void analyzeBloco(Bloco bloco) {
        // Implementar a lógica de análise semântica para blocos aqui
        
        for (DeclaracaoVariavel variavel : bloco.getDeclaracaoVariavel()) {
            analyzeDeclaracaoVariavel(variavel);
        }

        for (DeclaracaoSubRotina subRoutineDeclaration : bloco.getDeclaracaoSubRotina()) {
            analyzeDeclaracaoSubRotina(subRoutineDeclaration);
        }

        for (Comando cmd : bloco.getComandos()) {
            analyzeComando(cmd);
        }
    }

    private void analyzeDeclaracaoVariavel(DeclaracaoVariavel declaracao) {
        // Implementar a lógica de análise semântica para declarações de variáveis aqui

        // Validar se quando há atribuição, o tipo da variável é compatível com o valor atribuído
        if (declaracao.getValorInicializado().getClass() == ExpressaoFatorAtributo.class) {
            String tipoVariavel = declaracao.getTipo();
            String tipoValorInicializado = declaracao.getValorInicializado().toString().matches("\\w+\\{atributo=\\d+\\}") ? "integer" : "boolean";
            checkTypeCompatibility(declaracao.getIdentificador(), tipoVariavel, tipoValorInicializado);
        }
    }

    private void checkTypeCompatibility(String identificador, String tipoVariavel, String tipoValorAtribuido) {
        if (!tipoVariavel.toLowerCase().equals(tipoValorAtribuido.toLowerCase())) {
            throw new SemanticError("Tipo da variável " + identificador + " não é compatível com o valor inicializado. Esperado: " + tipoVariavel + ", encontrado: " + tipoValorAtribuido);
        }
    }

    private void analyzeDeclaracaoSubRotina(DeclaracaoSubRotina declaracao) {
        // Implementar a lógica de análise semântica para declarações de sub-rotinas aqui
        currentScope = "local";
        analyzeBloco(declaracao.getBloco());
        currentScope = "global";
    }

    private void analyzeComando(Comando comando) {
        // Implementar a lógica de análise semântica para comandos aqui

        if (comando instanceof ComandoAtribuicao) {
            analyzeComandoAtribuicao((ComandoAtribuicao) comando);
        } else if (comando instanceof ComandoCondicional) {
            analyzeComandoCondicional((ComandoCondicional) comando);
        } else if (comando instanceof ComandoEnquanto) {
            analyzeComandoEnquanto((ComandoEnquanto) comando);
        } else if (comando instanceof ComandoChamadaProcedure) {
            analyzeComandoChamadaProcedure((ComandoChamadaProcedure) comando);
        } else if (comando instanceof ComandoLeitura) {
            analyzeComandoLeitura((ComandoLeitura) comando);
        } else if (comando instanceof ComandoBreak) {
            analyzeComandoBreak((ComandoBreak) comando);
        } else if (comando instanceof ComandoContinue) {
            analyzeComandoContinue((ComandoContinue) comando);
        }

    }

    private void analyzeComandoContinue(ComandoContinue comando) {
        if (!inLoop) {
            throw new SemanticError("'continue' utilizado fora de um laço.");
        }
    }

    private void analyzeComandoBreak(ComandoBreak comando) {
        if (!inLoop) {
            throw new SemanticError("'break' utilizado fora de um laço.");
        }
    }

    private void analyzeComandoLeitura(ComandoLeitura comando) {
        Expressao expressao = comando.getExpressao();

        if (expressao instanceof ExpressaoFatorVariavel) {
            String identificador = ((ExpressaoFatorVariavel) expressao).getIdentificador();

            if (!symbolTable.contains(identificador)) {
                throw new SemanticError("Variável " + identificador + " não declarada.");
            }

        } else if (expressao instanceof ExpressaoFatorChamadaFunction) {
            String nomeFuncao = ((ExpressaoFatorChamadaFunction) expressao).getNome();

            if (!symbolTable.contains(nomeFuncao)) {
                throw new SemanticError("Função " + nomeFuncao + " não declarada.");
            }

        } else if (expressao instanceof ExpressaoCompleta) {
            analyzeExpressao(expressao);
        }
    }

    private void analyzeComandoChamadaProcedure(ComandoChamadaProcedure comando) {
        String nomeProcedure = comando.getNome();
    
        if (!symbolTable.contains(nomeProcedure)) {
            throw new SemanticError("Procedure '" + nomeProcedure + "' não declarada.");
        }
    
        String categoria = symbolTable.getEntry(nomeProcedure).getCategory();
        if (!categoria.equalsIgnoreCase("procedimento")) {
            throw new SemanticError("Identificador '" + nomeProcedure + "' não é uma procedure.");
        }
    
        // Verifica quantidade de parâmetros
        Object parametrosObj = symbolTable.getEntry(nomeProcedure).getParametros();
        List<?> parametrosEsperados = parametrosObj instanceof List ? (List<?>) parametrosObj : List.of();
        List<Expressao> argumentosRecebidos = comando.getParametros();
    
        if (parametrosEsperados.size() != argumentosRecebidos.size()) {
            throw new SemanticError("Procedure '" + nomeProcedure + "' espera " + parametrosEsperados.size() +
                    " argumento(s), mas recebeu " + argumentosRecebidos.size() + ".");
        }
    }
    
    private void analyzeComandoEnquanto(ComandoEnquanto comando) {
        boolean previousInLoop = inLoop;
        inLoop = true;
    
        for (Comando cmd : comando.getComando()) {
            analyzeComando(cmd);
        }
    
        inLoop = previousInLoop;
    }

    private void analyzeComandoCondicional(ComandoCondicional comando) {
        // 1. Verificar a expressão condicional (deve ser booleana)
        for (Expressao expressao : comando.getExpressao()) {
            String tipoExpressao = inferExpressionType(expressao);
            if (!"boolean".equalsIgnoreCase(tipoExpressao)) {
                throw new SemanticError("Condição do 'if' deve ser uma expressão booleana. Encontrado: " + tipoExpressao);
            }
        }
    
        // 2. Analisar os comandos do bloco if
        for (Comando cmd : comando.getComandosIf()) {
            analyzeComando(cmd);
        }
    
        // 3. Analisar os comandos do bloco else (se existir)
        if (comando.getComandosElse() != null && !comando.getComandosElse().isEmpty()) {
            for (Comando cmd : comando.getComandosElse()) {
                analyzeComando(cmd);
            }
        }
    }

    // Método auxiliar para inferir o tipo de uma expressão (Para o analyzeComandoCondicional)
    private String inferExpressionType(Expressao expressao) {
        if (expressao instanceof ExpressaoFatorAtributo) {
            Object valor = ((ExpressaoFatorAtributo) expressao).getAtributo();
            return (valor instanceof Boolean) ? "boolean" : "integer";
        } else if (expressao instanceof ExpressaoFatorVariavel) {
            String varName = ((ExpressaoFatorVariavel) expressao).getIdentificador();
            SymbolTableEntry entry = symbolTable.getEntry(varName);
            if (entry == null) {
                throw new SemanticError("Variável '" + varName + "' não declarada");
            }
            return entry.getTipo();
        } else if (expressao instanceof ExpressaoCompleta) {
            // Verifica operadores relacionais (que sempre retornam boolean)
            String operador = ((ExpressaoCompleta) expressao).getOperador();
            if (operador.equals("==") || operador.equals("!=") || 
                operador.equals("<") || operador.equals(">") ||
                operador.equals("<=") || operador.equals(">=")) {
                return "boolean";
            }
            // Para outros operadores, verificar compatibilidade de tipos
            String tipoEsquerda = inferExpressionType(((ExpressaoCompleta) expressao).getEsquerda());
            String tipoDireita = inferExpressionType(((ExpressaoCompleta) expressao).getDireita());
            
            if (!tipoEsquerda.equals(tipoDireita)) {
                throw new SemanticError("Tipos incompatíveis em expressão: " + 
                                    tipoEsquerda + " e " + tipoDireita);
            }
            return tipoEsquerda; // Retorna o tipo comum se forem iguais
        }
        return "unknown"; // Tipo padrão se não for possível determinar
    }

    private void analyzeComandoAtribuicao(ComandoAtribuicao comando) {
        // Implementar a lógica de análise semântica para atribuições aqui
        String identificadorAtribuicao = comando.getIdentificador();
        if (!symbolTable.contains(identificadorAtribuicao)) {
            throw new SemanticError("Variável " + identificadorAtribuicao + " não declarada.");
        }

        String tipoIdenficadorAtribuicao = symbolTable.getEntry(identificadorAtribuicao).getTipo();
        
        for (Expressao expressao : comando.getExpressao()) {
            if (expressao instanceof ExpressaoFatorVariavel) {
                ExpressaoFatorVariavel fatorVariavel = (ExpressaoFatorVariavel) expressao;
                
                String tipoVariavelAtribuida = symbolTable.getEntry(fatorVariavel.getIdentificador()).getTipo();
                checkTypeCompatibility(fatorVariavel.getIdentificador(), tipoIdenficadorAtribuicao, tipoVariavelAtribuida);
            } else if (expressao instanceof ExpressaoFatorAtributo) {
                ExpressaoFatorAtributo fatorAtributo = (ExpressaoFatorAtributo) expressao;
                
                String tipoAtributoAtribuido = fatorAtributo.getAtributo().getClass().getSimpleName();
                checkTypeCompatibility(fatorAtributo.getAtributo().toString(), tipoIdenficadorAtribuicao, tipoAtributoAtribuido);
            } else if (expressao instanceof ExpressaoFatorChamadaFunction) {
                ExpressaoFatorChamadaFunction fatorChamadaFunction = (ExpressaoFatorChamadaFunction) expressao;

                String tipoFuncaoAtribuida = symbolTable.getEntry(fatorChamadaFunction.getNome()).getTipo();
                checkTypeCompatibility(identificadorAtribuicao, tipoIdenficadorAtribuicao, tipoFuncaoAtribuida);
            } else if (expressao instanceof ExpressaoCompleta) {
                analyzeExpressao(expressao);
            }
        }
        
        //analise expressão [ExpressaoChamadaFunction{nome='', argumentos=''}]

        //analise expressão [ExpressaoCompleta{esquerda='', direita='', operador=''}]
             
    }

    private void analyzeExpressao(Expressao expressao) {
        // Implementar a lógica de análise semântica para expressões aqui
    }

    private void analyzeTermo(Expressao expressao) {
        // Implementar a lógica de análise semântica para expressões aqui
    }

    private void analyzeFator(Expressao expressao) {
        // Implementar a lógica de análise semântica para expressões aqui
    }
}
