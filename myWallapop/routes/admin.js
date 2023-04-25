module.exports = function (app, usersRepository) {
app.get("/users", function (req, res) {
    let filter = {};
    let options = {};

    let page = parseInt(req.query.page); // Es String !!!
    if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") {
        page = 1;
    }
    usersRepository.getUsersPg(filter, options, page).then(result => {
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
            users: result.users,
            pages: pages,
            currentPage: page
        }
        res.render("admin/users.twig", {user: req.session.user, response: response});
    }).catch(error => {
        res.send("Se ha producido un error al listar las canciones " + error)
    })
});
}