package br.com.precify.ui;

public class JOptionPaneDialogService implements DialogService {
    @Override
    public String escolherOpcao(String titulo, String mensagem, String[] opcoes) {
        return JOptionPaneUtils.escolherOpcao(titulo, mensagem, opcoes);
    }

    @Override
    public String solicitarTexto(String titulo, String mensagem) {
        return JOptionPaneUtils.solicitarTexto(titulo, mensagem);
    }

    @Override
    public Double solicitarDouble(String titulo, String mensagem, boolean permiteZero) {
        return JOptionPaneUtils.solicitarDouble(titulo, mensagem, permiteZero);
    }

    @Override
    public boolean confirmar(String titulo, String mensagem) {
        return JOptionPaneUtils.confirmar(titulo, mensagem);
    }

    @Override
    public void mostrarMensagem(String titulo, String mensagem) {
        JOptionPaneUtils.mostrarMensagem(titulo, mensagem);
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPaneUtils.mostrarErro(mensagem);
    }
}

