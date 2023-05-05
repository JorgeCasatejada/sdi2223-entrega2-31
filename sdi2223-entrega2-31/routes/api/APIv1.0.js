const {ObjectId} = require("mongodb");
const {offerAddValidator} = require("../validators/offerValidator");
const {validationResult} = require("express-validator");
module.exports = function (app, usersRepository, offersRepository, conversRepository, messagesRepository) {
    app.post('/api/v1.0/users/login', function(req, res){
        try {
            let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                .update(req.body.password).digest('hex');
            let filter = {
                email: req.body.email,
                password: securePassword
            }
            let options = {};
            usersRepository.findUser(filter, options).then(user => {
                if(user == null) {
                    res.status(401); //Unauthorized
                    res.json({
                        message: "Inicio de sesión incorrecto",
                        authenticated: false
                    })
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
                }
            }).catch(error => {
                res.status(401);
                res.json({
                    message: "Se ha producido un error al verificar las credenciales",
                    authenticated: false
                })
            })
        } catch(e) {
            res.status(500);
            res.json({
                message: "Se ha producido un error al verificar las credenciales",
                authenticated: false
            })
        }
    });

    app.get("/api/v1.0/offers/availablefromothers", function (req, res) {
        let filter = { author: { $ne: res.user } };
        offersRepository.getOffers(filter, {}).then(offers => {
            res.status(200);
            res.send({ offers: offers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las ofertas." })
        });
    });

    app.post('/api/v1.0/messages/send', function (req, res) {
        // validación pendiente

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
                                    owner: offer.author
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
                                        messagesRepository.insertMessage(newMess).then((messageId) => {
                                            if(messageId === null) {
                                                res.status(409);
                                                res.json({ error: "No se ha podido crear el mensaje. Ya existe." });
                                            } else {
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

    app.get("/api/v1.0/messages/fromconver/:conver", function (req, res) {
        // validación pendiente

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

    app.get("/api/v1.0/convers/all", function (req, res) {
        let filter = { $or: [{owner: res.user}, {offertant: res.user}] };
        conversRepository.getConvers(filter, {}).then(convers => {
            res.status(200);
            res.send({ convers: convers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las conversaciones." })
        });
    });

    app.delete("/api/v1.0/convers/delete", function (req, res) {

    });

    app.put("/api/v1.0/messages/markasread", function (req, res) {

    });

}