/*global cordova, module*/

module.exports = {
    //ACTION_CONFIGURE       : "configure",
    ACTION_START           : "start",
    ACTION_STOP            : "stop",
    ACTION_GET_STEPS       : "get_step_count",
    ACTION_CAN_COUNT_STEPS : "can_count_steps",

    start: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CordovaStepCounter", "start", []);
    },
 
    stop: function ( successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CordovaStepCounter", "stop", []);
    },
 
    getStepCount: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CordovaStepCounter", "get_step_count", []);
    },

 
    deviceCanCountSteps: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CordovaStepCounter", "can_count_steps", []);
    }
};
