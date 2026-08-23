const myTheme = agGrid.themeQuartz
  .withPart(agGrid.iconSetAlpine)
  .withParams({
    accentColor: "#E0C328",
    backgroundColor: "#F1EDE1",
    borderColor: "#98968F",
    borderRadius: 0,
    browserColorScheme: "dark",
    chromeBackgroundColor: {
      ref: "backgroundColor"
    },
    fontFamily: {
      googleFont: "Pixelify Sans"
    },
    fontSize: 15,
    foregroundColor: "#605E57",
    headerBackgroundColor: "#E4DAD1",
    headerFontSize: 15,
    headerFontWeight: 700,
    headerTextColor: "#3C3A35",
    rowVerticalPaddingScale: 1.2,
    spacing: 5,
    wrapperBorderRadius: 0
  });

const columnDefs = [
  {field: 'name', filter: 'agTextColumnFilter', flex: 2},
  {field: 'category', filter: 'agTextColumnFilter', flex: 1},
  {
    field: 'price',
    filter: 'agNumberColumnFilter',
    flex: 1,
    type: 'rightAligned',
    valueFormatter: (p) => (p.value != null ? '$' + Number(p.value).toFixed(2) : ''),
  },
  {field: 'createdAt', headerName: 'Created', filter: 'agDateColumnFilter', flex: 2},
];

const datasource = {
  getRows: async (params) => {
    try {
      const response = await fetch('/api/products/query', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          startRow: params.startRow,
          endRow: params.endRow,
          sortModel: params.sortModel,
          filterModel: params.filterModel,
        }),
      });
      if (!response.ok) {
        throw new Error('HTTP ' + response.status);
      }
      const data = await response.json();
      if (typeof params.success === 'function') {
        params.success({rowData: data.rows, rowCount: data.lastRow});
      } else {
        params.successCallback(data.rows, data.lastRow);
      }
    } catch (err) {
      console.error('getRows failed', err);
      if (typeof params.fail === 'function') {
        params.fail();
      }
    }
  }
};

const gridOptions = {
  columnDefs,
  defaultColDef: {sortable: true, resizable: true, floatingFilter: true},
  rowModelType: 'infinite',
  cacheBlockSize: 20,
  datasource,
  theme: myTheme,
  // Let ag-grid inject the theme's `googleFont: "Pixelify Sans"` link; keeps the font with the theme.
  loadThemeGoogleFonts: true
};

agGrid.ModuleRegistry.registerModules([agGrid.AllCommunityModule]);
agGrid.createGrid(document.querySelector('#grid'), gridOptions);
