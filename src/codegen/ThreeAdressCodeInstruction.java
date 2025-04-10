package codegen;

public class ThreeAdressCodeInstruction {
    private String operation;
    private String arg1;
    private String arg2;
    private String result;

    public ThreeAdressCodeInstruction(String operation, String arg1, String arg2, String result) {
        this.operation = operation;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    public String getOperation() {
        return operation;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        if (operation.equals("LABEL")) {
            return operation + " " + arg1 + ":"; // Exibe apenas o rótulo
        } else if (operation.equals("GOTO")) {
            return operation + " " + arg1; // Exibe apenas o destino do salto
        } else if (operation.equals("CALL")) {
            return operation + " " + arg1; // Exibe a chamada de função
        } else if (operation.equals("PARAM")) {
            return operation + " " + arg1; // Exibe o parâmetro
        } else if (operation.equals("IF_FALSE")) {
            return operation + " " + arg1 + " GOTO " + result; // Exibe a condição e o destino
        } else if (operation.equals("=")) {
            if (arg2 == null) {
                return result + " = " + arg1; // Atribuição simples
            } else if (arg1 == null) {
                return result + " = " + operation + " " + arg2; // Operação unária
            } else {
                return result + " = " + arg1 + " " + operation + " " + arg2; // Operação binária
            }
        } else if (arg2 == null) {
            return result + " = " + operation + " " + arg1; // Operação unária ou atribuição
        } else {
            return result + " = " + arg1 + " " + operation + " " + arg2; // Operação binária
        }
    }
}
