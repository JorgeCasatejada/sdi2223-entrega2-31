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
}
function formatDate(date) {
    let d = new Date();
}