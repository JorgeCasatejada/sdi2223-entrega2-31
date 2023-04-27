const express = require('express');
const adminSessionRouter = express.Router();
adminSessionRouter.use(function(req, res, next) {
    if ( req.session.user == "admin@email.com" ) {
        next();
    } else if (req.session.user) {
        res.redirect("/user/offers");
    } else {
        res.redirect("/users/login");
    }
});
module.exports = adminSessionRouter;