package org.example;

public class JsonParserUtil {

    public static DadosTelemetria parse(String jsonStr) {
        try {
            boolean alvoEncontrado = jsonStr.contains("\"alvo_encontrado\": true");

            int x = extrairValorInt(jsonStr, "\"x\":");
            int y = extrairValorInt(jsonStr, "\"y\":");

            return new DadosTelemetria(alvoEncontrado, x, y);
        } catch (Exception e) {
            return new DadosTelemetria(false, 0, 0);
        }
    }

    private static int extrairValorInt(String json, String chave) {
        int posInicio = json.indexOf(chave);
        if (posInicio == -1) return 0;

        posInicio += chave.length();
        int posFim = json.indexOf(",", posInicio);
        if (posFim == -1) {
            posFim = json.indexOf("}", posInicio);
        }

        String valorStr = json.substring(posInicio, posFim).trim();
        return Integer.parseInt(valorStr);
    }
}
