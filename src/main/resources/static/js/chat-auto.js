document.addEventListener("DOMContentLoaded", () => {
    const chat = document.getElementById("chatMensajes");
    if (!chat) return;

    const contador = document.getElementById("chatContador");
    let consultando = false;
    let enviando = false;

    function mostrarAviso(mensaje, esError = false) {
        document.getElementById("toastChat")?.remove();

        const aviso = document.createElement("div");
        aviso.id = "toastChat";
        aviso.setAttribute("role", esError ? "alert" : "status");
        aviso.textContent = mensaje;

        Object.assign(aviso.style, {
            position: "fixed",
            top: "85px",
            right: "20px",
            zIndex: "9999",
            maxWidth: "350px",
            padding: "14px 18px",
            borderRadius: "12px",
            background: esError ? "#fef2f2" : "#ecfdf5",
            color: esError ? "#991b1b" : "#065f46",
            border: `1px solid ${esError ? "#fecaca" : "#a7f3d0"}`,
            boxShadow: "0 8px 24px rgba(0,0,0,.12)",
            fontSize: "13px",
            fontWeight: "600",
            transition: "opacity .3s ease",
        });

        document.body.appendChild(aviso);

        setTimeout(() => {
            aviso.style.opacity = "0";
            setTimeout(() => aviso.remove(), 300);
        }, 4000);
    }

    // Mostrar avisos iniciales como notificaciones flotantes.
    document.querySelectorAll("[data-chat-feedback]").forEach((aviso) => {
        const esError = aviso.dataset.chatFeedback === "error";
        mostrarAviso(aviso.textContent.trim(), esError);
        aviso.remove();
    });

    // Quitar el espacio del banner si quedó vacío.
    const banner = document.getElementById("alertasSistema");
    if (banner && !banner.querySelector("div")) {
        banner.remove();
    }

    function actualizarDesde(documento) {
        const nuevo = documento.getElementById("chatMensajes");
        if (!nuevo) return false;

        if (chat.innerHTML !== nuevo.innerHTML) {
            const cercaDelFinal = chat.scrollHeight - chat.scrollTop - chat.clientHeight < 100;

            chat.innerHTML = nuevo.innerHTML;

            if (cercaDelFinal) {
                chat.scrollTop = chat.scrollHeight;
            }
        }

        const nuevoContador = documento.getElementById("chatContador");

        if (contador && nuevoContador) {
            contador.textContent = nuevoContador.textContent;
        }

        return true;
    }

    async function actualizarMensajes() {
        if (consultando || enviando || document.hidden) return;
        consultando = true;

        try {
            const respuesta = await fetch(location.pathname + "?chatSync=" + Date.now(), {
                credentials: "same-origin",
                cache: "no-store",
                headers: { Accept: "text/html" },
            });

            if (!respuesta.ok || respuesta.redirected) return;

            const html = await respuesta.text();
            const documento = new DOMParser().parseFromString(html, "text/html");

            actualizarDesde(documento);
        } catch (error) {
            console.warn("Error al actualizar mensajes:", error);
        } finally {
            consultando = false;
        }
    }

    // Envío asíncrono de respuestas públicas.
    const formularios = document.querySelectorAll('form[action$="/mensajes"]');

    formularios.forEach((form) => {
        // Las notas internas mantienen su envío tradicional.
        if (form.querySelector('[name="esNotaInterna"]')?.value === "true") {
            return;
        }

        form.addEventListener("submit", async (evento) => {
            evento.preventDefault();
            if (enviando) return;

            enviando = true;

            const boton = form.querySelector('[type="submit"]');
            const textarea = form.querySelector('textarea[name="contenido"]');

            if (boton) boton.disabled = true;

            try {
                const respuesta = await fetch(form.action, {
                    method: "POST",
                    body: new FormData(form),
                    credentials: "same-origin",
                    headers: { Accept: "text/html" },
                });

                if (!respuesta.ok || new URL(respuesta.url).pathname !== location.pathname) {
                    throw new Error("No se pudo completar el envío.");
                }

                const html = await respuesta.text();
                const documento = new DOMParser().parseFromString(html, "text/html");

                const errorServidor = documento.querySelector('[data-chat-feedback="error"]');

                const exitoServidor = documento.querySelector('[data-chat-feedback="success"]');

                if (errorServidor) {
                    mostrarAviso(errorServidor.textContent.trim(), true);
                } else if (exitoServidor && actualizarDesde(documento)) {
                    if (textarea) textarea.value = "";

                    mostrarAviso(exitoServidor.textContent.trim());
                    chat.scrollTop = chat.scrollHeight;
                } else {
                    mostrarAviso("No se pudo confirmar el envío. Revisa la conversación.", true);
                }
            } catch (error) {
                console.error("Error al enviar mensaje:", error);
                mostrarAviso("No se pudo enviar el mensaje. Inténtalo nuevamente.", true);
            } finally {
                enviando = false;
                if (boton) boton.disabled = false;
            }
        });
    });

    // Actualización automática cada 10 segundos.
    setInterval(actualizarMensajes, 10000);

    document.addEventListener("visibilitychange", () => {
        if (!document.hidden) actualizarMensajes();
    });

    chat.scrollTop = chat.scrollHeight;

    console.info("ServiClient: chat asíncrono activado");
});
