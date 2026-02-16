<%@page import="com.liferay.portal.kernel.upload.UploadException"%> <%@page
import="com.liferay.marketplace.exception.FileExtensionException"%> <%@page
import="com.liferay.portal.kernel.service.CompanyLocalServiceUtil"%> <%@ include
file="/init.jsp"%>

<portlet:resourceURL var="log">
  <portlet:param name="type" value="log" />
  <portlet:param name="mvcPath" value="/view.jsp" />
</portlet:resourceURL>
<portlet:resourceURL var="download">
  <portlet:param name="type" value="download" />
  <portlet:param name="mvcPath" value="/view.jsp" />
</portlet:resourceURL>

<portlet:resourceURL var="getLogsList" id="getLogsList">
  <portlet:param name="type" value="getLogsList" />
  <portlet:param name="mvcPath" value="/view.jsp" />
</portlet:resourceURL>

<div class="m-3">
  <div class="row">
    <!-- Colonna sinistra per i controlli -->
    <div id="config-left" class="col-4">
      <h2>Configurazione singoli log</h2>
      <div class="row mb-3">
        <div class="col">
          <div class="input-group">
            <div class="input-group-prepend">
              <span class="input-group-text w-100px">
                Limite
                <clay:icon symbol="lock" cssClass="ml-2" />
              </span>
            </div>
            <input
              class="form-control bg-white"
              type="number"
              step="1"
              min="0"
              id="limit"
              value="1000"
            />
          </div>
        </div>
      </div>

      <div class="row mb-3">
        <div class="col-12">
          <div class="input-group">
            <div class="input-group-prepend">
              <span class="input-group-text w-100px">
                Cerca
                <clay:icon symbol="search" cssClass="ml-2" />
              </span>
            </div>
            <input
              class="form-control bg-white"
              type="text"
              id="search"
              placeholder="Cerca del testo"
            />
          </div>
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-12">
          <input type="checkbox" id="follow" checked />
          <label for="follow">
            Segui i log
            <clay:icon symbol="magic" />
          </label>
        </div>
      </div>

      <hr />

      <h2>Configurazione file di log</h2>
      <div class="row mb-3">
        <div class="col">
          <div class="input-group">
            <div class="input-group-prepend">
              <span class="input-group-text w-100px">
                Limite (MB)
                <clay:icon symbol="lock" cssClass="ml-2" />
              </span>
            </div>
            <input
              class="form-control bg-white"
              type="number"
              step="0.1"
              min="0"
              id="maxSize"
              value="${sizeLog}"
            />
          </div>
        </div>
      </div>

      <div class="row mb-3">
        <div class="col d-flex flex-column">
          <div class="row align-items-center radio-fix">
            <div class="col">
              <label class="radio-inline mb-0">
                <input type="radio" name="filterOption" id="lastWeek" checked />
                Ultima settimana
              </label>
            </div>
          </div>
          <div class="row align-items-center radio-fix">
            <div class="col">
              <label class="radio-inline mb-0">
                <input type="radio" name="filterOption" id="allFiles" /> Tutti
              </label>
            </div>
          </div>
          <div class="row align-items-center">
            <div class="col-auto">
              <label class="radio-inline">
                <input type="radio" name="filterOption" id="dateRange" /> Range
                data
              </label>
            </div>
            <div class="col">
              <input
                class="form-control bg-white"
                type="date"
                id="startDate"
                placeholder="Data da"
                disabled
              />
            </div>
            <div class="col">
              <input
                class="form-control bg-white"
                type="date"
                id="endDate"
                placeholder="Data a"
                disabled
              />
            </div>
          </div>
        </div>
      </div>

      <div class="row mb-3">
        <div class="col">
          <select id="selectedLog" class="form-control bg-white">
            <c:forEach var="log" items="${logs}">
              <option value="${log}">${log}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-auto">
          <a
            href="#"
            class="btn btn-primary w-100"
            onmouseenter="downloadLog()"
            id="downloadLoadURL"
          >
            <clay:icon symbol="download" />
          </a>
        </div>
      </div>
    </div>
    <div class="col-8 position-relative">
      <div class="shadow" id="result"></div>
    </div>
  </div>
</div>
<script data-senna-track="temporary">
  var tml = setInterval(async function () {
    await log();
  }, 1000 * 10);

  var clearPortletHandlers = function (event) {
    clearInterval(tml);
  };

  Liferay.on("destroyPortlet", clearPortletHandlers);
</script>
<script>
  var configLeft = document.getElementById("config-left");
  var resultDiv = document.getElementById("result");
  var followCheckbox = document.getElementById("follow");
  var liveCheckbox = true;
  var limitInput = document.getElementById("limit");
  var searchInput = document.getElementById("search");
  var selectedLogSelect = document.getElementById("selectedLog");
  var downloadLoadHref = document.getElementById("downloadLoadURL");
  var usedTotal = document.getElementById("used-total");
  var usedMaximum = document.getElementById("used-maximum");

  // URL per recuperare la lista dei log filtrati
  var getLogsListURL = "<%= getLogsList %>";

  async function fetchLogsListWithFilter(lastWeek, allFiles) {
    var maxSizeInput = document.getElementById("maxSize");
    const maxSize = "&<portlet:namespace/>maxSize=" + maxSizeInput.value;
    var lastWeekParam =
      "&<portlet:namespace/>lastWeek=" + encodeURIComponent(lastWeek);
    var allFilesParam =
      "&<portlet:namespace/>allFiles=" + encodeURIComponent(allFiles);

    var url = getLogsListURL + maxSize + lastWeekParam + allFilesParam;

    await fetch(url, {
      headers: {
        "Strict-Transport-Security":
          "max-age=31536000; includeSubDomains; preload",
      },
    })
      .then((response) => response.json())
      .then((data) => {
        const selectedLogSelect = document.getElementById("selectedLog");
        selectedLogSelect.innerHTML = "";

        data.forEach(function (logName) {
          var option = document.createElement("option");
          option.value = logName;
          option.textContent = logName;
          selectedLogSelect.appendChild(option);
        });
        if (data.length === 0) {
          console.log("No logs available.");
        }
      })
      .catch((error) => {
        console.error("Errore nel recuperare la lista dei log:", error);
      });
  }

  // Evento che viene eseguito al caricamento della pagina
  document.addEventListener("DOMContentLoaded", function () {
    var maxSizeInput = document.getElementById("maxSize");
    const maxSize = "&<portlet:namespace/>maxSize=" + maxSizeInput.value;
    fetchLogsListWithFilter(true, false);
  });

  document.addEventListener("load", function () {
    var maxSizeInput = document.getElementById("maxSize");
    const maxSize = "&<portlet:namespace/>maxSize=" + maxSizeInput.value;
    fetchLogsListWithFilter(true, false); // Recupera i log in base al nuovo valore
  });

  document.getElementById("maxSize").addEventListener("input", function () {
    var maxSizeInput = document.getElementById("maxSize");
    if (isNaN(maxSizeInput.value)) {
      maxSizeInput.value = "";
      alert("Per favore, inserisci solo numeri ");
    }
  });

  var maxSizeInput = document.getElementById("maxSize");
  // Aggiunge un listener per l'evento di cambio sul campo maxSize
  maxSizeInput.addEventListener("change", function () {
    console.log("maxSize changed");

    var maxSize = "&<portlet:namespace/>maxSize=" + maxSizeInput.value;
    var startDate = document.getElementById("startDate").value;
    var endDate = document.getElementById("endDate").value;
    console.log("Start Date:", startDate);
    console.log("End Date:", endDate);
    if (document.getElementById("lastWeek").checked) {
      console.log("Fetching logs of LAST WEEK.");
      fetchLogsListWithFilter(true, false); // Recupera i log in base al nuovo valore
    } else if (document.getElementById("allFiles").checked) {
      console.log("Fetching logs of ALL FILES.");
      fetchLogsListWithFilter(false, true); // Recupera i log in base al nuovo valore
    } else {
      console.log("Fetching logs within date range.");
      fetchLogsListWithDateRange();
    }
  });

  document.getElementById("lastWeek").addEventListener("change", function () {
    if (this.checked) {
      document.getElementById("startDate").disabled = true;
      document.getElementById("endDate").disabled = true;

      // Svuota i campi di input delle date
      document.getElementById("startDate").value = "";
      document.getElementById("endDate").value = "";

      fetchLogsListWithFilter(true, false); // Mostra tutti i file
      // Mostra tutti i file senza filtro
    }
  });

  document.getElementById("allFiles").addEventListener("change", function () {
    if (this.checked) {
      document.getElementById("startDate").disabled = true;
      document.getElementById("endDate").disabled = true;

      // Svuota i campi di input delle date
      document.getElementById("startDate").value = "";
      document.getElementById("endDate").value = "";

      fetchLogsListWithFilter(false, true); // Mostra tutti i file
      // Mostra tutti i file senza filtro
    }
  });

  document.getElementById("dateRange").addEventListener("change", function () {
    if (this.checked) {
      document.getElementById("startDate").disabled = false;
      document.getElementById("endDate").disabled = false;

      // Ascolta il cambiamento delle date
      document
        .getElementById("startDate")
        .addEventListener("change", function () {
          fetchLogsListWithDateRange();
        });

      document
        .getElementById("endDate")
        .addEventListener("change", function () {
          fetchLogsListWithDateRange();
        });
    }
  });

  async function fetchLogsListWithDateRange() {
    var maxSizeInput = document.getElementById("maxSize");
    var startDate = document.getElementById("startDate").value;
    var endDate = document.getElementById("endDate").value;

    // Aggiungi log per vedere i valori di startDate e endDate
    console.log("Start Date:", startDate);
    console.log("End Date:", endDate);

    const startDateParam =
      "&<portlet:namespace/>startDate=" + encodeURIComponent(startDate);
    const endDateParam =
      "&<portlet:namespace/>endDate=" + encodeURIComponent(endDate);
    const maxSize = "&<portlet:namespace/>maxSize=" + maxSizeInput.value;

    var url = getLogsListURL + maxSize + startDateParam + endDateParam;

    console.log("URL costruito: " + url);
    await fetch(url)
      .then((response) => response.json())
      .then((data) => {
        const selectedLogSelect = document.getElementById("selectedLog");
        selectedLogSelect.innerHTML = ""; // Pulisce la lista esistente

        data.forEach(function (logName) {
          var option = document.createElement("option");
          option.value = logName;
          option.textContent = logName;
          selectedLogSelect.appendChild(option);
        });
        if (data.length === 0) {
          console.log("No logs available.");
        }
      })
      .catch((error) => {
        console.error("Errore nel recuperare la lista dei log:", error);
      });
  }

  //Funzione per impostare l'URL di download del log selezionato
  function downloadLog() {
    var selectedLog = document.getElementById("selectedLog").value;
    var downloadURL =
      "<%= download %>" + "&<portlet:namespace/>file=" + selectedLog;
    document.getElementById("downloadLoadURL").href = downloadURL;
  }

  function createDivWithHover(text) {
    var lines = text.split("\n");

    for (var i = 0; i < lines.length; i++) {
      var line = lines[i];
      var div = document.createElement("div");
      div.textContent = line;
      div.onmouseover = function () {
        this.classList.add("highlight");
      };
      div.onmouseout = function () {
        this.classList.remove("highlight");
      };

      const text = div.textContent || div.innerText;

      if (text.includes("ERROR")) div.classList.add("text-danger");
      if (text.includes("STARTED")) div.classList.add("text-success");

      if (text.includes("Caused by:")) div.classList.add("text-danger");
      if (text.includes("	at")) div.classList.add("text-danger");

      const regex = /\.{3}\s+(\d+)\s+more/;
      const match = text.match(regex);
      if (match) div.classList.add("text-danger");

      if (text.includes("it.almaviva") && text.includes("	at")) {
        div.classList.add("bg-danger");
        div.classList.remove("text-danger");
      }

      resultDiv.appendChild(div);
    }
  }

  async function log() {
    const limit = "&<portlet:namespace/>limit=" + limitInput.value;

    const response = await fetch("<%=log%>" + limit);
    const text = await response.text();

    resultDiv.innerHTML = "";

    createDivWithHover(text);

    if (followCheckbox.checked) resultDiv.scrollTop = resultDiv.scrollHeight;

    searchInLog();
  }

  limitInput.addEventListener("input", function () {
    if (Number(limitInput.value) < 0) limitInput.value = 0;
  });

  searchInput.addEventListener("input", function () {
    searchInLog();
  });

  function searchInLog() {
    const divs = resultDiv.querySelectorAll("div");
    divs.forEach(function (div) {
      const text = div.textContent || div.innerText;
      if (text.toLowerCase().includes(searchInput.value.toLowerCase())) {
        div.classList.remove("d-none");
      } else {
        div.classList.add("d-none");
      }
    });
  }
</script>
<style>
  #result {
    background-color: black;
    color: white;
    padding: 10px;
    border-radius: 5px;
    position: relative;
    height: 800px;
    overflow-y: auto;
  }

  .highlight {
    background-color: #333;
  }

  .w-100px {
    width: 100px;
  }

  #log-settings-move {
    border-right: 1px solid;
  }

  .c-p {
    cursor: move;
  }

  .row.align-items-center.radio-fix {
    height: 40px;
  }
</style>
