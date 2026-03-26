/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 56.33802816901409, "KoPercent": 43.66197183098591};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.5633802816901409, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.5, 500, 1500, "Create tag"], "isController": false}, {"data": [0.625, 500, 1500, "Get comments"], "isController": false}, {"data": [0.5555555555555556, 500, 1500, "Search tasks by project owner"], "isController": false}, {"data": [0.5, 500, 1500, "Create user"], "isController": false}, {"data": [0.5555555555555556, 500, 1500, "Get tasks with tags"], "isController": false}, {"data": [0.5, 500, 1500, "Create project"], "isController": false}, {"data": [0.5555555555555556, 500, 1500, "Get tasks by status"], "isController": false}, {"data": [0.5555555555555556, 500, 1500, "Get task by id"], "isController": false}, {"data": [0.5555555555555556, 500, 1500, "Search tasks by tag native"], "isController": false}, {"data": [0.5988023952095808, 500, 1500, "Create comment"], "isController": false}, {"data": [0.5555555555555556, 500, 1500, "Create task"], "isController": false}, {"data": [0.5649717514124294, 500, 1500, "Update task"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 1704, 744, 43.66197183098591, 2194.926643192487, 3, 5022, 27.0, 5007.0, 5007.0, 5008.0, 3.666479469651491, 11.233682626557018, 0.5512570508186104], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["Create tag", 40, 20, 50.0, 2508.824999999999, 5, 5008, 2513.0, 5007.0, 5008.0, 5008.0, 0.08709378912416309, 0.1256459739535137, 0.009846956480322248], "isController": false}, {"data": ["Get comments", 160, 60, 37.5, 1883.824999999999, 5, 5007, 13.0, 5007.0, 5007.0, 5007.0, 0.3906116490159028, 2.0682796219428536, 0.04434434003720576], "isController": false}, {"data": ["Search tasks by project owner", 180, 80, 44.44444444444444, 2231.772222222221, 5, 5009, 18.0, 5007.0, 5007.0, 5008.19, 0.418988561612268, 0.6593841581006783, 0.05569238150770706], "isController": false}, {"data": ["Create user", 40, 20, 50.0, 2517.9500000000003, 8, 5022, 2618.5, 5008.0, 5008.0, 5022.0, 0.0861461857699423, 0.1290552307425586, 0.014810582035935882], "isController": false}, {"data": ["Get tasks with tags", 180, 80, 44.44444444444444, 2233.9222222222224, 7, 5010, 25.5, 5007.0, 5007.0, 5008.38, 0.414171094078964, 4.858899691931486, 0.04336752449937069], "isController": false}, {"data": ["Create project", 40, 20, 50.0, 2510.724999999999, 8, 5008, 2529.0, 5007.0, 5008.0, 5008.0, 0.08805180968481854, 0.12985492226125853, 0.01343778960790529], "isController": false}, {"data": ["Get tasks by status", 180, 80, 44.44444444444444, 2238.8722222222214, 10, 5009, 42.0, 5006.0, 5007.0, 5009.0, 0.4094603540012238, 1.1812526906379164, 0.043318559586718014], "isController": false}, {"data": ["Get task by id", 180, 80, 44.44444444444444, 2229.56111111111, 3, 5007, 13.0, 5006.9, 5007.0, 5007.0, 0.4048601208282538, 0.5670809290077778, 0.041039531779270264], "isController": false}, {"data": ["Search tasks by tag native", 180, 80, 44.44444444444444, 2233.2388888888872, 7, 5007, 22.5, 5006.9, 5007.0, 5007.0, 0.4239204160072349, 0.7660949371243594, 0.05923432245153176], "isController": false}, {"data": ["Create comment", 167, 67, 40.119760479041915, 2017.065868263472, 7, 5008, 19.0, 5007.0, 5007.0, 5007.32, 0.40278330499378945, 0.5029209138296974, 0.06839694279632913], "isController": false}, {"data": ["Create task", 180, 80, 44.44444444444444, 2236.3999999999996, 10, 5007, 29.0, 5007.0, 5007.0, 5007.0, 0.400355871886121, 0.5607719014123665, 0.09563622525578291], "isController": false}, {"data": ["Update task", 177, 77, 43.50282485875706, 2190.4802259886987, 12, 5008, 34.0, 5007.0, 5007.0, 5008.0, 0.4218152353943496, 0.583258798243628, 0.10476027876744158], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 744, 100.0, 43.66197183098591], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 1704, 744, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 744, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["Create tag", 40, 20, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 20, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Get comments", 160, 60, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 60, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Search tasks by project owner", 180, 80, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 80, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Create user", 40, 20, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 20, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Get tasks with tags", 180, 80, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 80, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Create project", 40, 20, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 20, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Get tasks by status", 180, 80, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 80, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Get task by id", 180, 80, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 80, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Search tasks by tag native", 180, 80, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 80, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Create comment", 167, 67, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 67, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Create task", 180, 80, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 80, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Update task", 177, 77, "Non HTTP response code: java.net.SocketTimeoutException/Non HTTP response message: Read timed out", 77, "", "", "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
