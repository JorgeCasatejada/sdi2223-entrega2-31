module.exports = function(app, swig) {
    app.get("/offers", function(req, res) {
        res.send("lista de ofertas");
    });
    app.get('/add', function(req, res) {
        let response = req.query.num1 + req.query.num2;
        res.send(response);
    });
    app.get('/songs/:id', function(req, res) {
        let response = 'id: ' + req.params.id;
        res.send(response);
    });
    app.get('/songs/:kind/:id', function(req, res) {
        let response = 'id: ' + req.params.id + '<br>'
            + 'Tipo de música: ' + req.params.kind;
        res.send(response);
    });
};