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

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9587370141239641, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET /pcf/category/tree"], "isController": false}, {"data": [0.397, 500, 1500, "Flux Query - Daily Aggregation"], "isController": false}, {"data": [0.9954268292682927, 500, 1500, "Dashboard - energyConsumptionSummation"], "isController": false}, {"data": [1.0, 500, 1500, "GET /homepage/energyConsumptionRanking"], "isController": false}, {"data": [1.0, 500, 1500, "InfluxDB Write (500 points/batch)"], "isController": false}, {"data": [1.0, 500, 1500, "GET /consumptionanalysis/getByArea"], "isController": false}, {"data": [1.0, 500, 1500, "Carbon - carbonEmission/up"], "isController": false}, {"data": [1.0, 500, 1500, "POST /login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /carbonEmission/up"], "isController": false}, {"data": [1.0, 500, 1500, "GET /statisticalData/comprehensiveStatistics/getList"], "isController": false}, {"data": [1.0, 500, 1500, "GET /homepage/energyConsumptionSummation"], "isController": false}, {"data": [1.0, 500, 1500, "GET /consumptionanalysis/getYOY"], "isController": false}, {"data": [1.0, 500, 1500, "TC-06 Real-time Monitor"], "isController": true}, {"data": [0.9984756097560976, 500, 1500, "PCF - category/tree"], "isController": false}, {"data": [0.995, 500, 1500, "GET /homepage/peakValley"], "isController": false}, {"data": [1.0, 500, 1500, "TC-04 Product Carbon Footprint"], "isController": true}, {"data": [1.0, 500, 1500, "Budget - analysis"], "isController": false}, {"data": [0.9969040247678018, 500, 1500, "Dashboard - energyConsumptionTrend"], "isController": false}, {"data": [1.0, 500, 1500, "GET /pcf/config/params"], "isController": false}, {"data": [1.0, 500, 1500, "GET /pcf/config/factor/list"], "isController": false}, {"data": [0.9968152866242038, 500, 1500, "Energy - getByArea"], "isController": false}, {"data": [1.0, 500, 1500, "TC-02 Energy Analysis"], "isController": true}, {"data": [0.965, 500, 1500, "TC-01 Dashboard Load"], "isController": true}, {"data": [1.0, 500, 1500, "GET /homepage/energyConsumptionTrend"], "isController": false}, {"data": [1.0, 500, 1500, "GET /consumptionanalysis/getComprehensiveEnergy"], "isController": false}, {"data": [1.0, 500, 1500, "TC-03 Carbon Emission"], "isController": true}, {"data": [1.0, 500, 1500, "TC-05 Budget Analysis"], "isController": true}, {"data": [1.0, 500, 1500, "GET /rtdb/realtimeTrend/list"], "isController": false}, {"data": [0.0, 500, 1500, "Flux Query - Workshop Comparison"], "isController": false}, {"data": [0.498, 500, 1500, "Flux Query - Last 1h Snapshot"], "isController": false}, {"data": [1.0, 500, 1500, "GET /budget/analysis"], "isController": false}, {"data": [1.0, 500, 1500, "InfluxDB Write (100 points/batch)"], "isController": false}, {"data": [1.0, 500, 1500, "GET /carbonEmission/middle"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 25101, 0, 0.0, 166.76674236086185, 3, 19291, 19.0, 55.900000000001455, 947.9500000000007, 4820.950000000008, 24.62396002248441, 1326.6292141866863, 645.1309072540791], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /pcf/category/tree", 100, 0, 0.0, 151.54000000000002, 138, 199, 148.5, 163.9, 175.95, 198.95999999999998, 1.0535187526337968, 1.4753377844500632, 0.38683891698272227], "isController": false}, {"data": ["Flux Query - Daily Aggregation", 500, 0, 0.0, 1381.51, 443, 11578, 1334.5, 1583.8000000000002, 1730.75, 8643.13000000005, 0.7247626764615926, 1619.6095705160565, 0.31849922305441086], "isController": false}, {"data": ["Dashboard - energyConsumptionSummation", 328, 0, 0.0, 185.26524390243904, 135, 1074, 178.0, 204.0, 210.55, 529.2999999999973, 2.303969430259267, 2.6684645940307523, 0.8864882378146007], "isController": false}, {"data": ["GET /homepage/energyConsumptionRanking", 100, 0, 0.0, 5.839999999999999, 3, 11, 6.0, 8.0, 9.0, 10.989999999999995, 1.0525651011515063, 0.3741540007999495, 0.40293507778456095], "isController": false}, {"data": ["InfluxDB Write (500 points/batch)", 10000, 0, 0.0, 27.755399999999913, 9, 377, 24.0, 43.0, 54.0, 86.0, 123.19670818395733, 13.955877098963915, 6981.502254884749], "isController": false}, {"data": ["GET /consumptionanalysis/getByArea", 100, 0, 0.0, 50.28999999999997, 34, 153, 49.0, 56.0, 59.0, 152.08999999999952, 1.052343569128449, 5.050015916696483, 0.41209938595752743], "isController": false}, {"data": ["Carbon - carbonEmission/up", 376, 0, 0.0, 7.710106382978727, 4, 55, 7.0, 10.0, 11.0, 17.0, 2.8574685564464035, 0.9962072994642246, 1.0910841851274842], "isController": false}, {"data": ["POST /login", 101, 0, 0.0, 202.52475247524762, 141, 288, 198.0, 239.8, 253.79999999999998, 287.96000000000004, 0.4670671420577776, 0.25041002049777333, 0.11357394372303381], "isController": false}, {"data": ["GET /carbonEmission/up", 100, 0, 0.0, 8.230000000000002, 5, 31, 7.5, 11.0, 12.0, 30.85999999999993, 1.0547076877643362, 0.36770570755065235, 0.40272529874595253], "isController": false}, {"data": ["GET /statisticalData/comprehensiveStatistics/getList", 100, 0, 0.0, 27.37, 22, 47, 26.0, 32.0, 36.89999999999998, 46.949999999999974, 1.0543629539032515, 0.37479308127029654, 0.46437274629918607], "isController": false}, {"data": ["GET /homepage/energyConsumptionSummation", 100, 0, 0.0, 210.85999999999999, 198, 255, 208.0, 223.8, 239.5999999999999, 254.96999999999997, 1.0504312020084243, 1.2166127007636636, 0.4041698179602727], "isController": false}, {"data": ["GET /consumptionanalysis/getYOY", 100, 0, 0.0, 69.13999999999999, 62, 98, 68.0, 75.9, 83.74999999999994, 97.99, 1.0539629005059021, 0.4209676038153457, 0.41170425801011806], "isController": false}, {"data": ["TC-06 Real-time Monitor", 100, 0, 0.0, 10.21, 5, 136, 8.0, 11.900000000000006, 16.899999999999977, 135.05999999999952, 1.056669167450363, 0.37458096463328294, 0.3941871308262096], "isController": true}, {"data": ["PCF - category/tree", 328, 0, 0.0, 131.94512195121936, 99, 917, 128.0, 146.0, 149.55, 314.1899999999982, 2.345168809254837, 3.284152414522887, 0.8611166721482605], "isController": false}, {"data": ["GET /homepage/peakValley", 100, 0, 0.0, 117.07000000000002, 96, 567, 111.0, 125.9, 131.0, 562.7399999999978, 1.0514252068679095, 0.7495511728648183, 0.3881237580039744], "isController": false}, {"data": ["TC-04 Product Carbon Footprint", 100, 0, 0.0, 215.89000000000004, 189, 460, 209.5, 235.0, 246.84999999999997, 458.5499999999993, 1.052842147376844, 10.334098069613923, 1.1649122587675431], "isController": true}, {"data": ["Budget - analysis", 331, 0, 0.0, 25.302114803625383, 16, 292, 24.0, 28.0, 29.0, 67.76000000000005, 2.40673011902771, 1.5817669629937976, 0.9025237946353912], "isController": false}, {"data": ["Dashboard - energyConsumptionTrend", 323, 0, 0.0, 119.23529411764711, 85, 732, 112.0, 128.0, 140.00000000000006, 456.8799999999985, 2.3995780308602077, 0.8318849618704822, 0.9139017890971495], "isController": false}, {"data": ["GET /pcf/config/params", 100, 0, 0.0, 28.29, 23, 45, 27.5, 32.0, 33.94999999999999, 44.949999999999974, 1.0559662090813093, 0.9868746700105596, 0.3877375923970433], "isController": false}, {"data": ["GET /pcf/config/factor/list", 100, 0, 0.0, 36.05999999999999, 26, 220, 33.0, 42.900000000000006, 48.799999999999955, 218.60999999999927, 1.0553866936845662, 7.894787181273219, 0.39267805692755825], "isController": false}, {"data": ["Energy - getByArea", 314, 0, 0.0, 101.75796178343953, 20, 19291, 41.0, 49.0, 50.25, 59.100000000000136, 2.3078223417781993, 11.074842761228584, 0.9037468350127519], "isController": false}, {"data": ["TC-02 Energy Analysis", 100, 0, 0.0, 217.22000000000003, 195, 380, 211.0, 248.00000000000006, 256.9, 379.06999999999954, 1.0505525906626887, 6.254070891288817, 1.711251680884145], "isController": true}, {"data": ["TC-01 Dashboard Load", 100, 0, 0.0, 466.10000000000014, 434, 907, 457.0, 492.70000000000005, 512.9, 903.7799999999984, 1.0474714040306699, 2.6953976070515773, 1.5896196893199814], "isController": true}, {"data": ["GET /homepage/energyConsumptionTrend", 100, 0, 0.0, 132.32999999999996, 120, 194, 130.0, 144.0, 150.89999999999998, 193.5999999999998, 1.0510384259648533, 0.3643736730639872, 0.40029783801395774], "isController": false}, {"data": ["GET /consumptionanalysis/getComprehensiveEnergy", 100, 0, 0.0, 70.41999999999997, 63, 129, 68.0, 79.9, 85.84999999999997, 128.86999999999995, 1.0532746308272418, 0.42069269922689645, 0.42789281877356705], "isController": false}, {"data": ["TC-03 Carbon Emission", 100, 0, 0.0, 15.290000000000006, 9, 45, 14.0, 21.900000000000006, 22.94999999999999, 44.85999999999993, 1.0546631933092168, 0.7353803906472468, 0.8095363964268012], "isController": true}, {"data": ["TC-05 Budget Analysis", 100, 0, 0.0, 29.399999999999988, 24, 164, 27.0, 32.0, 38.94999999999999, 163.00999999999948, 1.056077727320731, 0.6940823344598163, 0.39602914774527403], "isController": true}, {"data": ["GET /rtdb/realtimeTrend/list", 100, 0, 0.0, 10.21, 5, 136, 8.0, 11.900000000000006, 16.899999999999977, 135.05999999999952, 1.056669167450363, 0.37458096463328294, 0.3941871308262096], "isController": false}, {"data": ["Flux Query - Workshop Comparison", 500, 0, 0.0, 4708.542000000002, 3553, 12472, 4581.0, 5434.8, 5766.299999999998, 12399.850000000048, 0.7324923344677198, 38.4581981160114, 0.34550175541787953], "isController": false}, {"data": ["Flux Query - Last 1h Snapshot", 500, 0, 0.0, 836.9179999999999, 591, 2758, 822.5, 975.0, 1080.6499999999999, 1365.8400000000001, 0.7236895429755797, 291.804676995194, 0.25724901722960064], "isController": false}, {"data": ["GET /budget/analysis", 100, 0, 0.0, 29.399999999999988, 24, 164, 27.0, 32.0, 38.94999999999999, 163.00999999999948, 1.056077727320731, 0.6940823344598163, 0.39602914774527403], "isController": false}, {"data": ["InfluxDB Write (100 points/batch)", 10000, 0, 0.0, 14.332299999999972, 4, 403, 12.0, 23.0, 31.0, 58.98999999999978, 121.65450121654501, 13.78117396593674, 1081.9766005170316], "isController": false}, {"data": ["GET /carbonEmission/middle", 100, 0, 0.0, 7.059999999999998, 4, 14, 6.0, 10.0, 11.949999999999989, 14.0, 1.0550081235625512, 0.3678104493279598, 0.4069611414132889], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 25101, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
