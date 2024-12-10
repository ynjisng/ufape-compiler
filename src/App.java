import java.util.List;

import lexer.*;
import parser.*;

public class App {
    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer();

        String sourceCode = """
                MODELO begin
                    var integer x_1 := 10;
                end
                """;
        List<Token> tokens = lexer.tokenize(sourceCode);

        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println('\n');

        // Exibe a tabela de símbolos
        //System.out.println("\n\n" + lexer.getSymbolTable());

        Parser parser = new Parser(tokens);
        ASTNode programa = parser.parsePrograma();

        System.out.println(programa.toString());

    }
}
