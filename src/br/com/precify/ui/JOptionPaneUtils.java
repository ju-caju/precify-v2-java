package br.com.precify.ui;

import javax.swing.JOptionPane;

public final class JOptionPaneUtils {
    private JOptionPaneUtils() {
    }

    public static String escolherOpcao(String titulo, String mensagem, String[] opcoes) {
        Object escolha = JOptionPane.showInputDialog(
                null,
                mensagem,
                titulo,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        return escolha == null ? null : escolha.toString();
    }

    public static String solicitarTexto(String titulo, String mensagem) {
        String valor = JOptionPane.showInputDialog(null, mensagem, titulo, JOptionPane.QUESTION_MESSAGE);
        if (valor == null) {
            return null;
        }

        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    public static Double solicitarDouble(String titulo, String mensagem, boolean permiteZero) {
        while (true) {
            String valor = JOptionPane.showInputDialog(null, mensagem, titulo, JOptionPane.QUESTION_MESSAGE);
            if (valor == null) {
                return null;
            }

            try {
                double numero = Double.parseDouble(valor.replace(',', '.').trim());
                if (numero < 0 || (!permiteZero && numero == 0.0)) {
                    mostrarErro("Informe um numero valido maior que " + (permiteZero ? "-1" : "0") + ".");
                    continue;
                }
                return numero;
            } catch (NumberFormatException erro) {
                mostrarErro("Valor numerico invalido.");
            }
        }
    }

    public static boolean confirmar(String titulo, String mensagem) {
        int resposta = JOptionPane.showConfirmDialog(
                null,
                mensagem,
                titulo,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return resposta == JOptionPane.YES_OPTION;
    }

    public static void mostrarMensagem(String titulo, String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}

