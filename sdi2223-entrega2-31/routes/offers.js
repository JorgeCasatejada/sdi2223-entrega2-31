const {ObjectId} = require("mongodb");
const {offerAddValidator} = require("./validators/offerValidator");
const {validationResult} = require("express-validator");

module.exports = function (app, usersRepository, offersRepository, logRepository, logger) {
    app.get('/offer/add', async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        getWallet(req.session.user).then(wallet => {
            res.render("offers/addOffer.twig", {user: req.session.user, wallet: wallet});
        }).catch(error => {
            res.send("Se ha producido un error al obtener el monedero " + error)
        })
    });


    app.post('/offer/add', offerAddValidator, async function (req, res){
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);// peticion
        // -----------------
        try {
            const errors = validationResult(req);
            if (!errors.isEmpty()) {
                let responseFail = "/offer/add?message=";
                errors.array().forEach(error => responseFail += error.msg + "<br>")
                res.redirect(responseFail + "&messageType=alert-danger");
            } else {
                let hasMoney = false;
                getWallet(req.session.user).then(wallet => {
                    hasMoney = wallet >= 20 ? true : false;
                    if (req.body.highlight ==='on' && !hasMoney)
                        res.redirect("/offer/add?message=Saldo insuficiente para destacar la oferta&messageType=alert-danger");
                    else {
                        let isHighlighted = req.body.highlight === 'on'? true : false;
                        let offer = {
                            title: req.body.title,
                            description: req.body.description,
                            date: new Date().toLocaleDateString('es-ES'),
                            price: req.body.price,
                            author: req.session.user,
                            sold: false,
                            highlighted:isHighlighted
                        }
                        offersRepository.insertOffer(offer).then(offerId => {
                            // si se destaca quitar dinero
                            if (isHighlighted){
                                usersRepository.decrementWallet(req.session.user, 20).then(result => {
                                    if (result.modifiedCount > 0)
                                        res.redirect("/user/offers" +
                                            "?message=Se ha añadido correctamente la oferta"+
                                            "&messageType=alert-info");
                                })
                                // si no se destaca no se quita dinero
                            } else {
                                res.redirect("/user/offers" +
                                    "?message=Se ha añadido correctamente la oferta"+
                                    "&messageType=alert-info");
                            }
                        }).catch(async error => {
                            await logRepository.insertLog('ALTA', logText);// alta de oferta
                            res.redirect("/offer/add" +
                                "?message=Se ha producido un error al añadir la oferta"+
                                "&messageType=alert-danger");
                        });
                    }
                }).catch(error => {
                    res.redirect("/offer/add" +
                        "?message=Se ha producido un error al añadir la oferta"+
                        "&messageType=alert-danger");
                });
            }
        } catch (e) {
            res.redirect("/offer/add" +
                "?message=Se ha producido un error al validar la oferta a añadir"+
                "&messageType=alert-danger");
        }
    });


    app.get('/offer/delete/:id', async function (req, res){
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        try {
            let filter = {_id: ObjectId(req.params.id)};

            userCanDeleteOffer(req.session.user, ObjectId(req.params.id)).then(canDelete => {
                if (canDelete) {
                    offersRepository.deleteOffer(filter, {}).then(result => {
                        if (result === null || result.deletedCount === 0) {
                            res.send("No se ha podido eliminar la oferta");
                        } else {
                            res.redirect("/user/offers" +
                                "?message=Se ha borrado correctamente la oferta"+
                                "&messageType=alert-info");
                        }
                    })
                } else {
                    res.redirect("/user/offers" +
                        "?message=No puedes eliminar esta oferta"+
                        "&messageType=alert-danger");
                }
            }).catch(error => {
                res.send("Se ha producido un error al comprobar si puede borrar la oferta " + error)
            });
        } catch (error) {
            res.send("Se ha producido un error al comprobar si puede borrar la oferta " + error)
        }
    });

    app.get('/offer/highlight/:id', async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        try {
            let user = req.session.user;
            const wallet = await getWallet(user);
            let filter = {_id: ObjectId(req.params.id)};
            const offer = await offersRepository.findOffer(filter, {});
            if (wallet < 20){
                res.redirect("/user/offers?message=Saldo insuficiente para destacar la oferta"+
                    "&messageType=alert-info");
            }
            else if (offer.highlighted){
                res.redirect("/user/offers?message=La oferta ya está destacada"+
                    "&messageType=alert-info");
            }
            else{
                await offersRepository.markOfferAsHighlighted(ObjectId(req.params.id))
                await usersRepository.decrementWallet(user, 20);
                res.redirect("/user/offers?message=Oferta destacada correctamente"+
                    "&messageType=alert-info");
            }
        } catch (e) {
            res.send("Ha habido un error al destacar " + e);
        }

    });


    // COMPRA DE OFERTAS
    app.get('/offer/buy/:id', async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        try {
            let filter = {_id: ObjectId(req.params.id)};
            const offer = await offersRepository.findOffer(filter, {});
            let user = req.session.user;
            console.log(user.email);
            const wallet = await getWallet(user);
            // validacion en servidor
            if (offer == null || typeof (offer) == 'undefined'){
                res.redirect("/offers?message=Ha habido un problema con la oferta"+
                    "&messageType=alert-info");
            }
            else if (offer.author == user){
                res.redirect("/offers?message=Un usuario no puede comprarse su oferta"+
                    "&messageType=alert-info");
            }
            else if (offer.sold){
                res.redirect("/offers?message=La oferta ya está comprada"+
                    "&messageType=alert-info");
            }
            else if (wallet < offer.price) {
                res.redirect("/offers?message=Saldo insuficiente en la cartera"+
                    "&messageType=alert-info");
            }
            else{
                let offerId = ObjectId(req.params.id);
                let purchase = {
                    user: req.session.user, // user es el email
                    offer: offer
                }
                await offersRepository.buyOffer(purchase);
                await offersRepository.markOfferAsSold(offerId);
                await usersRepository.decrementWallet(user, offer.price);
                res.redirect("/offers?message=Se ha comprado correctamente la oferta"+
                    "&messageType=alert-info");
            }
        } catch (error) {
            res.send("Ha habido un error en la compra " + error)
        }
    });

    app.get('/offers/purchases', async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        let filter = {user: req.session.user};
        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") {
            page = 1;
        }
        offersRepository.getPurchasesPg(filter, {}, page, 5).then(result => {
            let lastPage = result.total / 4;
            if (result.total % 4 > 0) { // Sobran decimales
                lastPage = lastPage + 1;
            }
            let pages = []; // paginas mostrar
            for (let i = page - 2; i <= page + 2; i++) {
                if (i > 0 && i <= lastPage) {
                    pages.push(i);
                }
            }
            let response = {
                purchases: result.purchases,
                pages: pages,
                currentPage: page
            }
            getWallet(req.session.user).then(wallet => {
                res.render("offers/purchases.twig", {user: req.session.user, response: response, wallet: wallet});
            }).catch(error => {
                res.send("Se ha producido un error al obtener el monedero " + error)
            })
        }).catch(error => {
            res.send("Se ha producido un error al listar las compras del usuario " + error)
        });
    })

    app.get('/offers', async function (req, res) {
        // -------- LOG ------------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------


        let filter = {};
        let options = {sort: {highlighted: -1, title: 1}};
        if(req.query.search != null && typeof(req.query.search) != "undefined" && req.query.search != ""){
            filter = {"title": {$regex: new RegExp(".*" + req.query.search + ".*", "i")}};
        }

        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") {
            page = 1;
        }
        offersRepository.getOffersPg(filter, options, page, 5).then(result => {
            let lastPage = result.total / 4;
            if (result.total % 4 > 0) { // Sobran decimales
                lastPage = lastPage + 1;
            }
            let pages = []; // paginas mostrar
            for (let i = page - 2; i <= page + 2; i++) {
                if (i > 0 && i <= lastPage) {
                    pages.push(i);
                }
            }
            let response = {
                offers: result.offers,
                pages: pages,
                currentPage: page,
                search: req.query.search
            }
            getWallet(req.session.user).then(wallet => {
                res.render("offers/allOffers.twig", {user: req.session.user, response: response, wallet: wallet});
            }).catch(error => {
                res.send("Se ha producido un error al obtener el monedero " + error)
            })
        }).catch(error => {
            res.send("Se ha producido un error al listar las canciones " + error)
        })
    });

    async function getWallet(user) {
        try {
            let filter = {email: user};
            let userObj = await usersRepository.findUser(filter, {});
            return userObj.wallet;
        } catch(error) {
            res.send("Se ha producido un error al obtener el monedero " + error);
        }
    }

    async function userCanDeleteOffer(user, offerId) {
        let filter = {author: user, _id: offerId};

        const offer = await offersRepository.findOffer(filter, {});
        if (offer == null || typeof (offer) == 'undefined'){
            return false;
        } else {
            if (!offer.sold){
                return true;
            } else {
                return false;
            }
        }
    }

}