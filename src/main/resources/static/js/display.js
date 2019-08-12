"use strict";

const OUTPUT_TABLE_NAME = "output";

function displayMsg(text, color) {
    if (color === undefined) {
        color = "black";
    }
    document.getElementById("msg").innerHTML = text;
    document.getElementById("msg").style.color = color;
}

function displayAllNodes(json) {
    cleanTable(OUTPUT_TABLE_NAME);
    var nodes;
    try {
        nodes = JSON.parse(json);
    } catch (e) {
        displayMsg("Invalid response from server " + json, "red");
        return;
    }
    for (var i in nodes) {
        displayNode(nodes[i]);
    }
}

function displayNode(jsonNode) {
    if (typeof jsonNode === "string") {
        var node;
        try {
            node = JSON.parse(jsonNode);
        } catch (e) {
            displayMsg("Invalid response from server " + jsonNode, "red");
            return;
        }
    } else {
        node = jsonNode;
    }

    var idx = 0;
    var table = document.getElementById(OUTPUT_TABLE_NAME);
    var row = table.insertRow(table.length);
    const nameCell = row.insertCell(idx++);
    nameCell.title = node.name;
    nameCell.innerHTML = node.name;
    row.insertCell(idx++).innerHTML = node.port;
    const chain = node.blockchain;
    const blockchainCell = row.insertCell(idx++);
    for (var i in chain) {
        blockchainCell.appendChild(createBlockP(chain[i]));
    }
    blockchainCell.className = "blockchain";
    var p = document.createElement("P");
    p.appendChild(addCellButton("Mine", function () {
        mine(node.name);
    }));
    p.appendChild(addCellButton("Delete", function () {
        deleteNode(node.name);
    }));
    p.appendChild(addCellButton("Abbreviate", function () {
        Abbreviate(node.name);
    }));
    p.appendChild(addCellButton("AddBlock", function () {
        AddBlock(node.name);
    }));
    row.insertCell(idx).appendChild(p);

    function addCellButton(name, onclick) {
        var button = document.createElement("BUTTON");
        button.className = "cellButton";
        button.appendChild(document.createTextNode(name));
        button.onclick = onclick;
        return button;
    }
}

function displayBlock(jsonBlock) {
    if (typeof jsonBlock === "string") {
        var block;
        try {
            block = JSON.parse(jsonBlock);
        } catch (e) {
            document.getElementById("msg").innerHTML = "Invalid response from server " + jsonBlock;
            return;
        }
    } else {
        block = jsonBlock;
    }
    displayMsg("New block mined:<br>" + getBlockString(block), "green");
}

function getBlockString(block) {
    return "index=" + block.id + " creator=" + block.Miner + " timestamp="
        + block.timeStamp + " hash=" + block.hash + " previous hash=" + block.previousHash + " difficulty=" + block.difficulty + "<br>";
}

function createBlockP(block) {
    var p = document.createElement("P");
    p.title = "creator " + block.Miner;
    p.innerHTML = "index=" + block.id + " creator=" + block.Miner + " timestamp="
        + block.timeStamp + " hash=" + block.hash + " previous hash=" + block.previousHash + " difficulty=" + block.difficulty;
    console.log("create p.innerHTML" + p.innerHTML);
    return p;
}

function cleanTable(name) {
    var table = document.getElementById(name);
    table.innerHTML = "";
    var row = table.insertRow(0);
    var idx = 0;
    row.insertCell(idx++).innerHTML = "Node name";
    row.insertCell(idx++).innerHTML = "Port";
    row.insertCell(idx++).innerHTML = "Blockchain";
    row.insertCell(idx).innerHTML = "Operations";
}