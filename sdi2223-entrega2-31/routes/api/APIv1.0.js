const {ObjectId} = require("mongodb");
const {offerAddValidator} = require("../validators/offerValidator");
const {validationResult} = require("express-validator");
module.exports = function (app, usersRepository, offersRepository, conversRepository, messagesRepository, logRepository, logger) {

    app.post('/api/v1.0/users/login', async function(req, res){
        try {
            // -------- LOG ------------
            const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
            logger.info(logText);
            await logRepository.insertLog('PET', logText);
            // -----------------
            let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                .update(req.body.password).digest('hex');
            let filter = {
                email: req.body.email,
                password: securePassword
            }
            let options = {};
            usersRepository.findUser(filter, options).then(async user => {
                if(user == null) {
                    res.status(401); //Unauthorized
                    res.json({
                        message: "Inicio de sesión incorrecto",
                        authenticated: false
                    })
                    // -------- LOG ------------
                    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                    Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
                    logger.info(logText);
                    await logRepository.insertLog('LOGIN-ERR', req.body.email);
                    // -----------------
                } else {
                    let token = app.get('jwt').sign(
                        {user: user.email, time: Date.now() / 1000},
                        "secreto");
                    res.status(200);
                    res.json({
                        message: "Usuario autenticado correctamente",
                        authenticated: true,
                        token: token
                    })
                    // -------- LOG ------------
                    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                    Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
                    logger.info(logText);
                    await logRepository.insertLog('LOGIN-EX', req.session.user);
                    // -----------------
                }
            }).catch(async error => {
                res.status(401);
                res.json({
                    message: "Se ha producido un error al verificar las credenciales",
                    authenticated: false
                })
                // -------- LOG ------------
                const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
                logger.info(logText);
                await logRepository.insertLog('LOGIN-ERR', req.body.email);
                // -----------------
            })
        } catch(e) {
            res.status(500);
            res.json({
                message: "Se ha producido un error al verificar las credenciales",
                authenticated: false
            })
            // -------- LOG ------------
            const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
            logger.info(logText);
            await logRepository.insertLog('LOGIN-ERR', req.body.email);
            // -----------------
        }
    });

    app.get("/api/v1.0/offers/availablefromothers", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        let filter = { author: { $ne: res.user } };
        offersRepository.getOffers(filter, {}).then(offers => {
            res.status(200);
            res.send({ offers: offers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las ofertas." })
        });
    });

    app.post('/api/v1.0/messages/send', async function (req, res) {
        // validación pendiente
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        try {
            if (req.body.conver !== null && typeof req.body.conver !== "undefined" && req.body.conver.trim() !== "") {
                let filter = {_id: ObjectId(req.body.conver)};
                conversRepository.findConver(filter, {}).then((conver) => {
                    if(conver !== null) {
                        let newMess = {
                            author: res.user,
                            text: req.body.text,
                            date: new Date(),
                            read: false,
                            idConver: conver._id
                        }
                        messagesRepository.insertMessage(newMess).then((messageId) => {
                            if(messageId === null) {
                                res.status(409);
                                res.json({ error: "No se ha podido crear el mensaje. Ya existe." });
                            } else {
                                res.status(200);
                                res.json({ message: "Mensaje enviado correctamente.",
                                    _idMess: messageId});
                            }
                        });
                    } else {
                        res.status(404);
                        res.json({ error: "La conversación en la que se quiere enviar el mensaje no existe."});
                    }
                })
            } else if (req.body.offer !== null && typeof req.body.offer !== "undefined" && req.body.offer.trim() !== "") {
                let filter = {_id: ObjectId(req.body.offer)};
                offersRepository.findOffer(filter, {}).then((offer) => {
                    if(offer !== null) {
                        let filter2 = {};
                        conversRepository.findConver(filter2, {}).then((conv) => {
                            if(conv === null) {
                                let newConv = {
                                    offertant: res.user,
                                    idOffer: offer._id,
                                    owner: offer.author,
                                    title: offer.title
                                }
                                conversRepository.insertConver(newConv).then((converId) => {
                                    if(converId !== null) {
                                        let newMess = {
                                            author: res.user,
                                            text: req.body.text,
                                            date: new Date(),
                                            read: false,
                                            idConver: converId
                                        }
                                        messagesRepository.insertMessage(newMess).then(async (messageId) => {
                                            if(messageId === null) {
                                                res.status(409);
                                                res.json({ error: "No se ha podido crear el mensaje. Ya existe." });
                                            } else {
                                                // -------- LOG ------------
                                                const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -
                                                Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
                                                logger.info(logText);
                                                await logRepository.insertLog('ALTA', logText);
                                                // -----------------
                                                res.status(200);
                                                res.json({ message: "Mensaje enviado correctamente.",
                                                    _idConv: converId,
                                                    _idMess: messageId});
                                            }
                                        });
                                    } else {
                                        res.status(409);
                                        res.json({ error: "No se ha podido crear la conversación. Ya existe." });
                                    }
                                });
                            } else {
                                res.status(409);
                                res.json({ error: "No se ha podido crear la conversación. Ya existe." });
                            }
                        })
                    } else {
                        res.status(404);
                        res.json({ error: "La oferta para la que se quiere abrir conversación no existe." });
                    }
                })
            } else {
                res.status(400);
                res.json({ error: "Se necesita conversación u oferta para enviar el mensaje." });
            }
        } catch(e) {
            res.status(500);
            res.json({ error: "Se ha producido un error al enviar el mensaje." })
        }
    });

    app.get("/api/v1.0/messages/fromconver/:conver", async function (req, res) {
        // validación pendiente
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        try {
            if (req.params.conver !== null && typeof req.params.conver !== "undefined" && req.params.conver.trim() !== "") {
                let filter = {idConver: ObjectId(req.params.conver)};
                let options = { sort: { date: 1} }; // 1 -> asc (antiguo -> actual); -1 -> desc (actual -> antiguo)
                messagesRepository.findMessages(filter, options).then((messages) => {
                    if(messages.length > 0) {
                        res.status(200);
                        res.json({ messages: messages });
                    } else {
                        res.status(404);
                        res.json({ error: "La conversación no existe." });
                    }
                })
            } else {
                res.status(400);
                res.json({ error: "Se necesita una conversación para obtener los mensajes." });
            }
        } catch(e) {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar la conversación." })
        }
    });

    app.get("/api/v1.0/convers/all", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        let filter = { $or: [{owner: res.user}, {offertant: res.user}] };
        conversRepository.getConvers(filter, {}).then(convers => {
            res.status(200);
            res.send({ convers: convers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las conversaciones." })
        });
    });

    app.delete("/api/v1.0/convers/delete/:conver", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        // validación pendiente
        try {
            if (req.params.conver !== null && typeof req.params.conver !== "undefined" && req.params.conver.trim() !== "") {
                let filter = {_id: ObjectId(req.params.conver)};
                conversRepository.deleteConver(filter, {}).then((result) => {
                    if(result === null |result.deletedCount === 0) {
                        res.status(404);
                        res.json({error: "La conversación que se quiere borrar, no existe." });
                    } else {
                        let filter2 = {idConver: ObjectId(req.params.conver)};
                        messagesRepository.deleteMessages(filter2, {}).then((result2) => {
                            if(result2 === null | result2.deletedCount === 0) {
                                res.status(404);
                                res.json({error: "La conversación que se quiere borrar, no tiene mensajes." });
                            } else {
                                res.status(200);
                                res.send(JSON.stringify(result2));
                            }
                        });
                    }
                })
            } else {
                res.status(400);
                res.json({ error: "Se necesita una conversación para poder eliminarla." });
            }
        } catch(e) {
            res.status(500);
            res.json({ error: "Se ha producido un error al eliminar la conversación." })
        }
    });

    app.put("/api/v1.0/messages/markasread/:message", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        // validación pendiente

        try {
            if (req.params.message !== null && typeof req.params.message !== "undefined" && req.params.message.trim() !== "") {
                let filter = {_id: ObjectId(req.params.message)};
                let options = {upsert: false};
                let newInfo = {
                    read: true
                }
                messagesRepository.updateMessage(newInfo, filter, options).then((result) => {
                    console.log(result);
                    if (result === null || result.matchedCount == 0) {
                        res.status(404);
                        res.json({error: "El mensaje que se quiere marcar como leído, no existe."});
                    } else if (result.modifiedCount === 0) {
                        res.status(409);
                        res.json({error: "El mensaje ya se había leído"});
                    } else {
                        res.status(200);
                        res.json({message: "Mensaje marcado a leído", result: result});
                    }
                });
            } else {
                res.status(400);
                res.json({ error: "Se necesita un mensaje para poder leerlo." });
            }
        } catch(e) {
            res.status(500);
            res.json({error: "Se ha producido un error al intentar leer el mensaje."});
        }
    });

    app.get("/api/v1.0/offerConversation/:offerId", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        let filter = { offertant: res.user, idOffer: ObjectId(req.params.offerId)};
        conversRepository.getConvers(filter, {}).then(convers => {
            console.log(convers);
            res.status(200);
            res.send({ convers: convers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las conversaciones." })
        });
    });

}