const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository) {
    app.get('/offer/add', function (req, res) {
        res.render("offers/addOffer.twig", {user: req.session.user});
    });
    app.post('/offer/add', function (req, res){
        //Validación en el servidor
        let responseFail = "/offer/add?message=";
        if (req.body.title === null || typeof (req.body.title) == 'undefined' || req.body.title.trim().length == 0)
            responseFail += "El título proporcionado no es válido<br>";
        if (req.body.description === null || typeof (req.body.description) == 'undefined' || req.body.description.trim().length == 0)
            responseFail += "La descripción proporcionada no es válida<br>";
        if (req.body.price === null || typeof (req.body.price) == 'undefined' || req.body.price <= 0)
            responseFail += "El precio proporcionado no es válido, debe ser positivo<br>";
        if (responseFail.length > 20){
            res.redirect(responseFail + "&messageType=alert-danger");
        } else {
            let offer = {
                title: req.body.title,
                description: req.body.description,
                date: new Date().toLocaleDateString('es-ES'),
                price: req.body.price,
                author: req.session.user
            }
            offersRepository.insertOffer(offer).then(offerId => {
                res.redirect("/user/offers" +
                    "?message=Se ha añadido correctamente la oferta"+
                    "&messageType=alert-info");
            }).catch(error => {
                res.redirect("/offer/add" +
                    "?message=Se ha producido un error al añadir la oferta"+
                    "&messageType=alert-danger");
            });
        }
    });
    app.get('/offer/delete/:id', function (req, res){
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
    });
    app.get('/offers', function (req, res) {
        let filter = {};
        let options = {sort: {title: 1}};
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
            res.render("offers/allOffers.twig", {user: req.session.user, response: response});
        }).catch(error => {
            res.send("Se ha producido un error al listar las canciones " + error)
        })
    });

    async function userCanDeleteOffer(user, offerId) {
        let filter = {author: user, _id: offerId};

        const offer = await offersRepository.findOffer(filter, {});
        if (offer == null || typeof (offer) == 'undefined'){
            return false;
        } else {
            if (offer.state == "Disponible"){
                return true;
            } else {
                return false;
            }
        }
    }
}