// script.js

// Função para buscar dados
async function fetchData() {
    try {
        const response = await fetch('http://localhost:8080/v1/nodes');
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Erro ao obter os dados:', error);
    }
}

// Função para fechar o modal
function closeModal() {
    d3.select(".modal").style("display", "none");
}

// Função para adicionar um novo nó
function addNewNode() {
    const newNodeCity = d3.select("#newCity").property("value");
    const newPrice = parseFloat(d3.select("#newPrice").property("value"));
    const newStartMoment = parseDate(d3.select("#startMoment").property("value"));
    const newEndMoment = parseDate(d3.select("#endMoment").property("value"));

    if (isNaN(newPrice) || !isFinite(newPrice)) {
        alert("O preço deve ser um número válido.");
        return;
    }

    d3.select(".modal").style("display", "none");
}

// Função para exibir o modal
function showPopup(d) {
    const destinationText = `Adicionar novo destino saindo de ${d.data.currentCity}`;
    d3.select("#destinationText").text(destinationText);
    d3.select(".modal").style("display", "block");
    d3.select("#newCity").property("value", "");
    d3.select("#newPrice").property("value", "");
    d3.select("#startMoment").property("value", "");
    d3.select("#endMoment").property("value", "");
    d3.select(".modal").datum(d);
}

// Função para renderizar o gráfico
function renderChart() {
    // ... Seu código existente para renderizar o gráfico ...
}

// Função para analisar a data
function parseDate(dateString) {
    const parsedDate = new Date(dateString);
    return !isNaN(parsedDate) ? parsedDate : null;
}

// Aguarda o DOM ser totalmente carregado
document.addEventListener("DOMContentLoaded", function () {
    // Inicializa o DateTimePicker diretamente nos elementos de entrada
    $("#startMoment").datetimepicker({
        dateFormat: 'yy-mm-dd',
        timeFormat: 'HH:mm:ss',
        showSecond: true
    });

    $("#endMoment").datetimepicker({
        dateFormat: 'yy-mm-dd',
        timeFormat: 'HH:mm:ss',
        showSecond: true
    });

    // Chama a função para renderizar o gráfico quando a página é carregada
    renderChart();

    // Adiciona o evento de clique ao botão de fechar
    document.getElementById("closeButton").addEventListener("click", closeModal);
    
    // Adiciona o evento de clique ao botão de adicionar
    document.querySelector(".btn-primary").addEventListener("click", addNewNode);
});
