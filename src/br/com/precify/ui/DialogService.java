package br.com.precify.ui;

public interface DialogService {
    String escolherOpcao(String titulo, String mensagem, String[] opcoes);

    String solicitarTexto(String titulo, String mensagem);

    Double solicitarDouble(String titulo, String mensagem, boolean permiteZero);

    boolean confirmar(String titulo, String mensagem);

    void mostrarMensagem(String titulo, String mensagem);

    void mostrarErro(String mensagem);
}

