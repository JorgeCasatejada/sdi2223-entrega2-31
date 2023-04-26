module.exports = function (app, usersRepository) {
    app.get("/admin/users", function (req, res) {
        let filter = {email: {$ne: 'admin@email.com'}};

        let options = {sort: {email: 1}};

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
    app.post('/admin/delete', function (req, res){
        if (typeof (req.body.id) == 'string'){
            if (req.body.id == "admin@email.com" || req.session.user == req.body.id){
                res.send("No se ha podido eliminar el registro");
            } else {
                let filter = {email: req.body.id}
                usersRepository.deleteUser(filter, {}).then(result => {
                    if (result === null || result.deletedCount === 0) {
                        res.send("No se ha podido eliminar el registro");
                    } else {
                        res.redirect("/users");
                    }
                }).catch(error => {
                    res.send("Se ha producido un error al intentar eliminar el usuario: " + error)
                });
            }
        } else {
            if (req.body.id.includes("admin@email.com") || req.body.id.includes(req.session.user)){
                res.send("No se ha podido eliminar el registro");
            } else {
                let filter = {email: { $in: req.body.id}}
                usersRepository.deleteUsers(filter, {}).then(result => {
                    if (result === null || result.deletedCount === 0) {
                        res.send("No se ha podido eliminar el registro");
                    } else {
                        res.redirect("/users");
                    }
                }).catch(error => {
                    res.send("Se ha producido un error al intentar eliminar el usuario: " + error)
                });
            }
        }
    });
}