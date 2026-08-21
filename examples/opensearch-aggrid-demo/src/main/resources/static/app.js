const columnDefs = [
  { field: 'name', filter: 'agTextColumnFilter', flex: 2 },
  { field: 'category', filter: 'agTextColumnFilter', flex: 1 },
  {
    field: 'price',
    filter: false,
    flex: 1,
    type: 'rightAligned',
    valueFormatter: (p) => (p.value != null ? '$' + Number(p.value).toFixed(2) : ''),
  },
  { field: 'createdAt', headerName: 'Created', filter: 'agDateColumnFilter', flex: 2 },
];

const datasource = {
  getRows: async (params) => {
    try {
      const response = await fetch('/api/products/query', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          startRow: params.startRow,
          endRow: params.endRow,
          sortModel: params.sortModel,
          filterModel: params.filterModel,
        }),
      });
      if (!response.ok) throw new Error('HTTP ' + response.status);
      const data = await response.json();
      // ag-grid renamed the infinite datasource callbacks across versions
      // (successCallback/failCallback -> success/fail). Support both so the demo
      // works regardless of the exact community build served by the CDN.
      if (typeof params.success === 'function') {
        params.success({ rowData: data.rows, rowCount: data.lastRow });
      } else {
        params.successCallback(data.rows, data.lastRow);
      }
    } catch (err) {
      console.error('getRows failed', err);
      if (typeof params.fail === 'function') {
        params.fail();
      } else if (typeof params.failCallback === 'function') {
        params.failCallback();
      }
    }
  },
};

const gridOptions = {
  columnDefs,
  defaultColDef: { sortable: true, resizable: true, floatingFilter: true },
  rowModelType: 'infinite',
  cacheBlockSize: 20,
  datasource,
};

agGrid.ModuleRegistry.registerModules([agGrid.AllCommunityModule]);
agGrid.createGrid(document.querySelector('#grid'), gridOptions);
