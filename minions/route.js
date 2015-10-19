var mockRouter = require('./routes/mockRouter.js');
var monitorRouter = require('./routes/monitorRouter.js');

module.exports = function (app) {
    app.use('/mock', mockRouter);//
    app.use('/m', monitorRouter);


    app.get("/", function (req, res, next) {
        res.render("index");
    });


};