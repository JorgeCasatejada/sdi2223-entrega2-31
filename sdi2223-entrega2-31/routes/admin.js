module.exports = function (app, usersRepository, logRepository, logger) {
    app.get("/admin/users", async function (req, res) {
        // ----- LOG -------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

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

    app.post('/admin/delete', async function (req, res){
        // ----- LOG -------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------

        if (req.session.user == "admin@email.com") {
            if (typeof (req.body.id) == 'string'){
                if (req.body.id == "admin@email.com"){
                    res.send("No se ha podido eliminar el registro de administrador");
                } else {
                    let filter = {email: req.body.id}
                    usersRepository.deleteUser(filter, {}).then(result => {
                        if (result === null || result.deletedCount === 0) {
                            res.send("No se ha podido eliminar el registro");
                        } else {
                            res.redirect("/admin/users" +
                                "?message=Se ha borrado correctamente el usuario"+
                                "&messageType=alert-info");
                        }
                    }).catch(error => {
                        res.send("Se ha producido un error al intentar eliminar el usuario: " + error)
                    });
                }
            } else {
                if (req.body.id.includes("admin@email.com")){
                    res.send("No se ha podido eliminar el registro");
                } else {
                    let filter = {email: { $in: req.body.id}}
                    usersRepository.deleteUsers(filter, {}).then(result => {
                        if (result === null || result.deletedCount === 0) {
                            res.send("No se ha podido eliminar el registro");
                        } else {
                            res.redirect("/admin/users" +
                                "?message=Se han borrado correctamente los usuarios"+
                                "&messageType=alert-info");
                        }
                    }).catch(error => {
                        res.send("Se ha producido un error al intentar eliminar los usuarios: " + error)
                    });
                }
            }
        } else {
            res.send("Usted no puede eliminar usuarios");
        }
    });

    app.get('/admin/logs', async function (req, res) {
        // ----- LOG -------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        let filter = {};
        let options = {sort: { timestamp: -1}};
        const typeToFilter = req.query.tipo;
        if (typeToFilter && typeToFilter !== '') {
            filter.type = typeToFilter;
        }
        logRepository.getLogs(filter, options).then(logs => {
            res.render("admin/logs.twig", {user: req.session.user, logs: logs});
        }).catch(error => {
            res.send("Se ha producido un error al listar los logs " + error)
        });
    })

    app.post('/admin/logs/delete', async function(req, res) {
        // ----- LOG -------
        const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
        logger.info(logText);
        await logRepository.insertLog('PET', logText);
        // -----------------
        const result = await logRepository.deleteLogs({},{});
        if (result.deletedCount > 0){
            res.redirect("/admin/logs" +
                "?message=Se han borrado correctamente los logs"+
                "&messageType=alert-info");
        }
        else{
            res.redirect("/admin/logs" +
                "?message=No se han podido borrar los logs"+
                "&messageType=alert-info");
        }
    })

}