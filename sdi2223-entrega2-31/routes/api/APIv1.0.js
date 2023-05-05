const {ObjectId} = require("mongodb");
const {offerAddValidator} = require("../validators/offerValidator");
const {validationResult} = require("express-validator");
module.exports = function (app, usersRepository, offersRepository) {
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

    app.get("/api/v1.0/offers/available-from-others", function (req, res) {
        let filter = { author: { $ne: res.user }, sold: false };
        let options = {};
        offersRepository.getOffers(filter, options).then(offers => {
            res.status(200);
            res.send({ offers: offers })
        }).catch(error => {
            res.status(500);
            res.json({ error: "Se ha producido un error al recuperar las ofertas." })
        });
    });

    app.post('/api/v1.0/messages/from-conver', function (req, res) {

    });

    app.get("/api/v1.0/messages/send", function (req, res) {

    });

    app.get("/api/v1.0/convers/all", function (req, res) {

    });

    app.delete("/api/v1.0/convers/delete", function (req, res) {

    });

    app.put("/api/v1.0/messages/markAsRead", function (req, res) {

    });

}