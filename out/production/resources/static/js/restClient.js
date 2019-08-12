"use strict";

function getAllNodes() {
    sendHttpRequest("GET", "node/all", null, displayAllNodes);
}

function deleteAllNodes() {
    sendHttpRequest("DELETE", "node/all", null, getAllNodes);
}

function createNode() {
    var idx = getNextCount();
    var name = "Node" + idx;
    var port = 3000 + idx;
    sendHttpRequest("POST", "node?name=" + name + "&port=" + port + "&rogue=false", null, displayNode);
}

function Miner() {
    var name = "Miner" + Math.floor(Math.random() * 5);
    sendHttpRequest("POST", "node/miner?miner=" + name, null, getAllNodes);
}

function miner(name) {
    sendHttpRequest("POST", "node/miner?miner=" + name, null, getAllNodes);
}

function deleteNode(name) {
    sendHttpRequest("DELETE", "node?name=" + name, null, getAllNodes);
}

function getNode() {
    var name = document.getElementById("nodeNameGet").value;
    sendHttpRequest("GET", "node?name=" + name, null, null);
}

function mine(name) {
    sendHttpRequest("POST", "node/mine?node=" + name, null, getAllNodes);
}

function Abbreviate(name) {
    sendHttpRequest("POST", "node/abbreviate?node=" + name, null, getAllNodes);
}

function AddBlock(name){
    sendHttpRequest("POST", "node/addblocks?node=" + name, null, getAllNodes);
}

function sendHttpRequest(action, url, data, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4 && xmlHttp.status === 200) {
            callback(xmlHttp.responseText);
        }
    };
    xmlHttp.open(action, url, true);
    xmlHttp.send(data);
}

var getNextCount = (function () {
    if (!sessionStorage.count) {
        sessionStorage.count = 1;
    }
    return function () {
        sessionStorage.count = Number(sessionStorage.count) + 1;
        return Number(sessionStorage.count);
    }
})();