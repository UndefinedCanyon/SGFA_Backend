package com.sgfa.backend.application.usecase;

final class PlantillaCorreo {

    private PlantillaCorreo() {
    }

    static String construir(String nombre, String titulo, String texto,
                            String textoBoton, String enlace, String nota) {
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f4f7f5;padding:24px;">
                  <div style="max-width:480px;margin:0 auto;background:#ffffff;border-radius:12px;padding:28px;border:1px solid #e5e7eb;">
                    <p style="margin:0 0 4px;color:#15803d;font-weight:bold;font-size:18px;">SGFA</p>
                    <h1 style="margin:0 0 16px;font-size:20px;color:#111827;">%s</h1>
                    <p style="margin:0 0 12px;color:#374151;">Hola, %s.</p>
                    <p style="margin:0 0 20px;color:#374151;">%s</p>
                    <p style="margin:0 0 24px;"><a href="%s" style="background:#16a34a;color:#ffffff;text-decoration:none;padding:12px 20px;border-radius:8px;display:inline-block;font-weight:bold;">%s</a></p>
                    <p style="margin:0 0 8px;color:#6b7280;font-size:13px;">Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
                    <p style="margin:0 0 20px;color:#6b7280;font-size:13px;word-break:break-all;">%s</p>
                    <p style="margin:0;color:#6b7280;font-size:13px;">%s</p>
                  </div>
                </div>
                """.formatted(
                escapar(titulo), escapar(nombre), escapar(texto),
                escapar(enlace), escapar(textoBoton), escapar(enlace), escapar(nota));
    }

    static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}