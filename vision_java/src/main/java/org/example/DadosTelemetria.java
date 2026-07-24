package org.example;

public class DadosTelemetria {
    private boolean alvoEncontrado;
    private int x;
    private int y;

    public DadosTelemetria(boolean alvoEncontrado, int x, int y) {
        this.alvoEncontrado = alvoEncontrado;
        this.x = x;
        this.y = y;
    }

    public boolean isAlvoEncontrado() {
        return alvoEncontrado;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("Alvo Detectado: %b | Posição: X=%d, Y=%d", alvoEncontrado, x, y);
    }
}