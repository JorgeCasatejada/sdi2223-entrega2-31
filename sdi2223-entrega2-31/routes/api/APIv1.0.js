const {ObjectId} = require("mongodb");
const {userLoginValidator} = require("../validators/userValidator");
const {firstMessageValidator} = require("../validators/messageValidator");
const {validationResult} = require("express-validator");

module.exports = function (app, usersRepository, offersRepository, conversRepository, messagesRepository, logRepository, logger) {

    app.post('/api/v1.0/users/login', userLoginValidator, async function(req, res){
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        try {
            // Validación en el servidor
            const errors = validationResult(req);
            // Si hay error en la validación: Devolver JSON con los errores
            if (!errors.isEmpty()){
                res.status(500);
                res.json({
                    errors: errors.array(),
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
            // Si no hay error en la validación: Seguir la ejecución
            else {
                let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                    .update(req.body.password).digest('hex');
                let filter = {
                    email: req.body.email,
                    password: securePassword
                }
                usersRepository.findUser(filter, {}).then(async user => {
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
            }
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

    app.post('/api/v1.0/messages/send', firstMessageValidator, async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        try {
            // Validación en el servidor
            const errors = validationResult(req);
            // Si hay error en la validación: Devolver JSON con los errores
            if (!errors.isEmpty()){
                res.status(500);
                res.json({
                    errors: errors.array(),
                    message: "Se ha producido un error al enviar el mensaje."
                })
            }
            // Si no hay error en la validación: Seguir la ejecución
            else {
                // Si la petición contiene información sobre la conversación a la que va el mensaje (La conver ya existe)
                if (req.body.conver !== null && typeof req.body.conver !== "undefined" && req.body.conver.trim() !== "") {
                    let filter = {_id: ObjectId(req.body.conver)};
                    conversRepository.findConver(filter, {}).then((conver) => {
                        // Si la conversación existe en el sistema
                        if(conver !== null) {
                            // VALIDACIÓN: Usuario trata de enviar mensaje a una conversación ajena
                            if(conver.offertant !== res.user && conver.owner !== res.user) {
                                res.status(500);
                                res.json({ error: "El usuario no puede enviar un mensaje a una conversación ajena." });
                            } else {
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
                            }
                        }
                        // Si la conversación no existe en el sistema
                        else {
                            res.status(404);
                            res.json({ error: "La conversación en la que se quiere enviar el mensaje no existe."});
                        }
                    })
                }
                // Si la petición contiene información sobre la oferta a la que va el mensaje (La conver no existe)
                else if (req.body.offer !== null && typeof req.body.offer !== "undefined" && req.body.offer.trim() !== "") {
                    let filter = {_id: ObjectId(req.body.offer)};
                    offersRepository.findOffer(filter, {}).then((offer) => {
                        // Si la oferta existe en el sistema
                        if(offer !== null) {
                            // VALIDACIÓN: Propietario trata de iniciar una conversación por un producto suyo
                            if(offer.author === res.user) {
                                res.status(500);
                                res.json({ error: "El usuario no puede iniciar una conversación por un producto propio." });
                            } else {
                                let filter2 = { offertant: res.user, idOffer: offer._id };
                                // Comprobación de que la oferta no tenga una conversación ya asignada con el usuario interesado
                                conversRepository.findConver(filter2, {}).then((conv) => {
                                    // Si la conversación para esa oferta y ofertante no existía
                                    if(conv === null) {
                                        let newConv = {
                                            offertant: res.user,
                                            idOffer: offer._id,
                                            owner: offer.author
                                        }
                                        // Crear la conversación
                                        conversRepository.insertConver(newConv).then((converId) => {
                                            if(converId !== null) {
                                                let newMess = {
                                                    author: res.user,
                                                    text: req.body.text,
                                                    date: new Date(),
                                                    read: false,
                                                    idConver: converId
                                                }
                                                // Crear el mensaje
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
                                                            _idMess: messageId
                                                        });
                                                    }
                                                });
                                            } else {
                                                res.status(409);
                                                res.json({ error: "No se ha podido crear la conversación. Ya existe." });
                                            }
                                        });
                                    }
                                    // Si la conversación para esa oferta y ofertante ya existía
                                    else {
                                        res.status(409);
                                        res.json({ error: "No se ha podido crear la conversación. Ya existe." });
                                    }
                                })
                            }
                        }
                        // Si la oferta no existe en el sistema
                        else {
                            res.status(404);
                            res.json({ error: "La oferta para la que se quiere abrir conversación no existe." });
                        }
                    })
                }
                // Si la petición no contiene información sobre la conversación ni oferta a la que va el mensaje
                else {
                    res.status(400);
                    res.json({ error: "Se necesita conversación u oferta para enviar el mensaje." });
                }
            }
        } catch(e) {
            res.status(500);
            res.json({ error: "Se ha producido un error al enviar el mensaje." })
        }
    });

    app.get("/api/v1.0/messages/fromconver/:conver", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        try {
            if (req.params.conver !== null && typeof req.params.conver !== "undefined" && req.params.conver.trim() !== "") {
                let filter = {_id: ObjectId(req.params.conver)};
                conversRepository.findConver(filter, {}).then((conver) => {
                    if(conver === null) {
                        res.status(404);
                        res.json({ error: "La conversación no existe." });
                    }
                    // VALIDACIÓN: Acceso a una conversación ajena
                    else if(conver.owner !== res.user && conver.offertant !== res.user) {
                        res.status(500);
                        res.json({ error: "El usuario no puede leer una conversación ajena." });
                    }
                    else {
                        let filter2 = {idConver: ObjectId(req.params.conver)};
                        let options = { sort: { date: 1} }; // 1 -> asc (antiguo -> actual); -1 -> desc (actual -> antiguo)
                        messagesRepository.findMessages(filter2, options).then((messages) => {
                            res.status(200);
                            res.json({ messages: messages });
                        })
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

        /*
        let filter = { $or: [{owner: res.user}, {offertant: res.user}] };
        conversRepository.getConvers(filter, {}).then(convers => {
            res.status(200);
            res.send({ convers: convers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las conversaciones." })
        });
        */

        let filter = { $or: [{owner: res.user}, {offertant: res.user}] };
        const conversPromise = conversRepository.getConvers(filter, {});

        conversPromise.then(async (convers) => {
            const offerIds = convers.map((conver) => conver.idOffer);
            const filter2 = {_id: {$in: offerIds}};
            const options2 = {projection: {title: 1}};
            const offersPromise = offersRepository.getOffers(filter2, options2);

            const [offers] = await Promise.all([offersPromise]);
            const offerMap = {};
            offers.forEach((offer) => (offerMap[offer._id] = offer.title));

            const result = convers.map((conver) => ({
                _id: conver._id,
                offertant: conver.offertant,
                idOffer: conver.idOffer,
                owner: conver.owner,
                offerTitle: offerMap[conver.idOffer],
            }));

            res.status(200);
            res.send({ convers: result });
        }).catch((error) => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las conversaciones." });
        });
    });

    app.delete("/api/v1.0/convers/delete/:conver", async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        try {
            if (req.params.conver !== null && typeof req.params.conver !== "undefined" && req.params.conver.trim() !== "") {
                let filter = {_id: ObjectId(req.params.conver)};
                conversRepository.findConver(filter, {}).then((conver) => {
                    if(conver === null) {
                        res.status(404);
                        res.json({error: "La conversación que se quiere borrar, no existe." });
                    }
                    // VALIDACIÓN: Borrado de una conversación ajena
                    else if(conver.owner !== res.user && conver.offertant !== res.user) {
                        res.status(500);
                        res.json({ error: "El usuario no puede borrar una conversación ajena." });
                    }
                    else {
                        conversRepository.deleteConver(filter, {}).then((result) => {
                            if(result === null | result.deletedCount === 0) {
                                res.status(404);
                                res.json({error: "La conversación que se quiere borrar, no existe." });
                            } else {
                                let filter2 = {idConver: conver._id};
                                messagesRepository.deleteMessages(filter2, {}).then((result2) => {
                                    if (result2 === null | result2.deletedCount === 0) {
                                        res.status(404);
                                        res.json({error: "La conversación que se quiere borrar, no tiene mensajes."});
                                    } else {
                                        res.status(200);
                                        res.send(JSON.stringify(result2));
                                    }
                                });
                            }
                        })
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

        try {
            if (req.params.message !== null && typeof req.params.message !== "undefined" && req.params.message.trim() !== "") {
                let filter = {_id: ObjectId(req.params.message)};
                messagesRepository.findMessage(filter, {}).then((message) => {
                    if(message === null) {
                        res.status(404);
                        res.json({error: "El mensaje que se quiere marcar como leído, no existe."});
                    } else {
                        let filter2 = {_id: message.idConver}
                        conversRepository.findConver(filter2, {}).then((conver) => {
                            // VALIDACIÓN: Usuario ajeno a la conversación quiere marcar un mensaje como leído
                            if(conver.offertant !== res.user && conver.owner !== res.user) {
                                res.status(500);
                                res.json({ error: "El usuario no puede marcar como leído un mensaje de una conversación ajena." });
                            }
                            // VALIDACIÓN: Un usuario de la conversación quiere marcar como leído un mensaje escrito por él
                            else if(message.author === res.user) {
                                res.status(500);
                                res.json({ error: "El usuario no puede marcar como leído un mensaje escrito por él." });
                            }
                            else {
                                let options = {upsert: false};
                                let newInfo = {read: true};
                                messagesRepository.updateMessage(newInfo, filter, options).then((result) => {
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
                            }
                        })
                    }
                })
            } else {
                res.status(400);
                res.json({ error: "Se necesita un mensaje para poder leerlo." });
            }
        } catch(e) {
            res.status(500);
            res.json({error: "Se ha producido un error al intentar leer el mensaje."});
        }
    });

    //Metodo creado adicionalmente para poder reanudar conversaciones desde el apartado offers
    app.get("/api/v1.0/convers/:offerId", async function(req, res) {
        try {
            if(req.params.offerId !== null && typeof req.params.offerId !== "undefined" && req.params.offerId.trim() !== "") {
                let filter = { _id: ObjectId(req.params.offerId) };
                offersRepository.findOffer(filter, {}).then((offer) => {
                    if(offer !== null) {
                        let filter2 = {idOffer: ObjectId(req.params.offerId), offertant: res.user};
                        conversRepository.findConver(filter2, {}).then(conver => {
                            res.status(200);
                            res.send({conver: conver})
                        }).catch(error => {
                            res.status(500);
                            res.json({error: "Se ha producido un error al intentar acceder a la conversación de la oferta."})
                        });
                    } else {
                        res.status(404);
                        res.json({error: "La oferta cuya conversación se quiere acceder, no existe."});
                    }
                })
            } else {
                res.status(400);
                res.json({ error: "Se necesita una oferta para acceder a la conversación." });
            }
        } catch(e) {
            res.status(500);
            res.json({error: "Se ha producido un error al intentar acceder a la conversación de la oferta."});
        }
    });

}