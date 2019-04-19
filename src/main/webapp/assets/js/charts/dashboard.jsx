/* eslint-disable react/prop-types */
/* eslint-disable react/no-string-refs */
let dashid = null
let dash_editable = false
$(document).ready(function () {
  win_resize(100)

  let d = $urlp('d')
  if (d) $storage.set('DashDefault', d)

  let dash_list = null
  $.get(rb.baseUrl + '/dashboard/dash-gets', ((res) => {
    dash_list = res.data
    let d = dash_list[0]  // default
    if (res.data.length > 1) {
      let dset = $storage.get('DashDefault')
      if (dset) {
        for (let i = 0; i < res.data.length; i++) {
          if (res.data[i][0] === dset) {
            d = res.data[i]
            break
          }
        }
      }
    }

    dashid = d[0]
    dash_editable = d[3]
    render_dashboard(d[2])
    $('.dash-list h4').text(d[1])

    if (location.hash && location.hash.length > 20) {
      if (location.hash.substr(0, 5) === '#del=') {
        rb.hbsuccess('仪表盘已删除')
        location.hash = ''
      } else {
        let high = $('#chart-' + location.hash.substr(1)).addClass('high')
        if (high.length > 0) {
          high.on('mouseleave', () => {
            high.removeClass('high').off('mouseleave')
          })
          $gotoSection(high.offset().top - 115, '.chart-grid')
        }
      }
    }

    if (dash_editable !== true) $('.J_dash-edit, .J_chart-adds').remove()

    $('.J_dash-new').click(() => { show_dlg('DlgDashAdd') })
    $('.J_dash-edit').click(() => { show_dlg('DlgDashSettings', { title: d[1], shareToAll: d[4] === 'ALL' }) })
    $('.J_chart-new').click(() => { show_dlg('DlgAddChart') })
    $('.J_dash-select').click(() => { show_dlg('DashSelect', { dashList: dash_list }) })
    $('.J_chart-select').click(() => {
      let dlg = show_dlg('ChartSelect')
      let appended = []
      $('.grid-stack-item-content').each(function () {
        let chid = $(this).attr('id').substr(6)
        appended.push(chid)
      })
      dlg.setState({ appended: appended })
    })
  }))

  $(window).resize(win_resize)
})

let on_resizestart = false
let rendered_charts = []
let win_resize = function (t) {
  if (on_resizestart === true) return
  $setTimeout(() => {
    let cg = $('.chart-grid')
    if ($(window).width() >= 768) cg.height($(window).height() - 142)
    else cg.height('auto')
    $(rendered_charts).each((idx, item) => { item.resize() })
  }, t || 400, 'resize-charts')
}

const dlg_cached = {}
const show_dlg = (t, props) => {
  props = props || {}
  props.dashid = props.dashid || dashid
  if (dlg_cached[t]) dlg_cached[t].show()
  else if (t === 'DlgAddChart') dlg_cached[t] = renderRbcomp(<DlgAddChart {...props} />)
  else if (t === 'DlgDashAdd') dlg_cached[t] = renderRbcomp(<DlgDashAdd {...props} />)
  else if (t === 'DlgDashSettings') dlg_cached[t] = renderRbcomp(<DlgDashSettings {...props} />)
  else if (t === 'DashSelect') dlg_cached[t] = renderRbcomp(<DashSelect {...props} />)
  else if (t === 'ChartSelect') dlg_cached[t] = renderRbcomp(<ChartSelect {...props} />)
  return dlg_cached[t]
}

let gridstack
let gridstack_serialize
let render_dashboard = function (init) {
  gridstack = $('.grid-stack').gridstack({
    cellHeight: 60,
    handleClass: 'chart-title',
    animate: true,
    auto: false,
    verticalMargin: 20
  }).data('gridstack')

  gridstack_serialize = init
  $(init).each((idx, item) => { add_widget(item) })
  if (rendered_charts.length === 0) {
    let gsi = '<div class="grid-stack-item"><div id="chart-add" class="grid-stack-item-content"><a class="chart-add" onclick="show_dlg(\'DlgAddChart\')"><i class="zmdi zmdi-plus"></i><p>添加图表</p></a></div></div>'
    gridstack.addWidget(gsi, 0, 0, 2, 2)
    gridstack.disable()
  }

  // When resize/re-postion/remove
  $('.grid-stack').on('change', function () {
    save_dashboard()
  }).on('resizestart', function () {
    on_resizestart = true
  }).on('gsresizestop', function () {
    $(rendered_charts).each((idx, item) => { item.resize() })
    on_resizestart = false
  })

  $('.chart-grid').removeClass('invisible')
  $('.J_dash-load').remove()
}

let add_widget = function (item) {
  let chid = 'chart-' + item.chart
  if ($('#' + chid).length > 0) return false

  let chart_add = $('#chart-add')
  if (chart_add.length > 0) gridstack.removeWidget(chart_add.parent())

  let gsi = '<div class="grid-stack-item"><div id="' + chid + '" class="grid-stack-item-content"></div></div>'
  // Use gridstar
  if (item.size_x || item.size_y) {
    gridstack.addWidget(gsi, (item.col || 1) - 1, (item.row || 1) - 1, item.size_x || 2, item.size_y || 2, 2, 12, 2, 12)
  } else {
    gridstack.addWidget(gsi, item.x, item.y, item.w, item.h, item.x === undefined, 2, 12, 2, 12)
  }
  // eslint-disable-next-line no-undef
  let c = renderRbcomp(detectChart(item, item.chart, dash_editable), chid)
  rendered_charts.push(c)
}

let save_dashboard = function () {
  if (dash_editable !== true) return
  let s = []
  $('.chart-grid .grid-stack-item').each(function () {
    let $this = $(this)
    let chid = $this.find('.grid-stack-item-content').attr('id')
    if (chid && chid.length > 20) {
      s.push({
        x: $this.attr('data-gs-x'),
        y: $this.attr('data-gs-y'),
        w: $this.attr('data-gs-width'),
        h: $this.attr('data-gs-height'),
        chart: chid.substr(6)
      })
    }
  })
  gridstack_serialize = s
  $setTimeout(() => {
    $.post(rb.baseUrl + '/dashboard/dash-config?id=' + dashid, JSON.stringify(gridstack_serialize), () => {
      // eslint-disable-next-line no-console
      console.log('Saved dashboard: ' + JSON.stringify(gridstack_serialize))
    })
  }, 500, 'save-dashboard')
}

// 添加图表
class DlgAddChart extends RbFormHandler {
  constructor(props) {
    super(props)
  }
  render() {
    return (<RbModal title="添加图表" ref="dlg">
      <div className="form">
        <div className="form-group row">
          <label className="col-sm-3 col-form-label text-sm-right">图表数据来源</label>
          <div className="col-sm-7">
            <select className="form-control form-control-sm" ref="entity" />
          </div>
        </div>
        <div className="form-group row footer">
          <div className="col-sm-7 offset-sm-3">
            <button className="btn btn-primary" type="button" onClick={() => this.next()}>下一步</button>
          </div>
        </div>
      </div>
    </RbModal>)
  }
  componentDidMount() {
    let entity_el = $(this.refs['entity'])
    $.get(rb.baseUrl + '/commons/metadata/entities', (res) => {
      $(res.data).each(function () {
        $('<option value="' + this.name + '">' + this.label + '</option>').appendTo(entity_el)
      })
      this.__select2 = entity_el.select2({ placeholder: '选择数据来源' })
    })
  }
  next() {
    let e = this.__select2.val()
    if (!e) return
    location.href = rb.baseUrl + '/dashboard/chart-design?source=' + e + '&dashid=' + this.props.dashid
  }
}

// 面板设置
class DlgDashSettings extends RbFormHandler {
  constructor(props) {
    super(props)
  }
  render() {
    return (<RbModal title="仪表盘设置" ref="dlg">
      <div className="form">
        <div className="form-group row">
          <label className="col-sm-3 col-form-label text-sm-right">名称</label>
          <div className="col-sm-7">
            <input className="form-control form-control-sm" value={this.state.title || ''} placeholder="默认仪表盘" data-id="title" onChange={this.handleChange} maxLength="40" />
          </div>
        </div>
        {rb.isAdminUser !== true ? null :
          <div className="form-group row">
            <label className="col-sm-3 col-form-label text-sm-right"></label>
            <div className="col-sm-7">
              <label className="custom-control custom-control-sm custom-checkbox custom-control-inline mt-0 mb-0">
                <input className="custom-control-input" type="checkbox" checked={this.state.shareToAll === true} data-id="shareToAll" onChange={this.handleChange} />
                <span className="custom-control-label">共享此仪表盘给全部用户</span>
              </label>
            </div>
          </div>
        }
        <div className="form-group row footer">
          <div className="col-sm-7 offset-sm-3">
            <button className="btn btn-primary btn-space" type="button" onClick={() => this.save()}>确定</button>
            <button className="btn btn-secondary btn-space" type="button" onClick={() => this.delete()}><i className="zmdi zmdi-delete icon" /> 删除</button>
          </div>
        </div>
      </div>
    </RbModal >)
  }
  save() {
    let _data = { shareTo: this.state.shareToAll === true ? 'ALL' : 'SELF', title: this.state.title || '默认仪表盘' }
    _data.metadata = { id: this.props.dashid, entity: 'DashboardConfig' }
    $.post(rb.baseUrl + '/dashboard/dash-update', JSON.stringify(_data), (res) => {
      if (res.error_code === 0) {
        // rb.hbsuccess('设置已保存')
        $('.dash-head h4').text(_data.title)
        if (dlg_cached['DashSelect']) {
          dlg_cached['DashSelect'].setState({ 'dashTitle': _data.title })
        }
        this.hide()
      } else rb.hberror(res.error_msg)
    })
  }
  delete() {
    rb.alert('确认删除此仪表盘？', {
      confirm: function () {
        $.post(rb.baseUrl + '/dashboard/dash-delete?id=' + dashid, function (res) {
          // if (res.error_code === 0) location.replace('home#del=' + dashid)  // Chrome no refresh?
          if (res.error_code === 0) location.reload()
          else rb.hberror(res.error_msg)
        })
      }
    })
  }
}

// 添加面板
class DlgDashAdd extends RbFormHandler {
  constructor(props) {
    super(props)
  }
  render() {
    return (<RbModal title="添加仪表盘" ref="dlg">
      <div className="form">
        <div className="form-group row">
          <label className="col-sm-3 col-form-label text-sm-right">名称</label>
          <div className="col-sm-7">
            <input className="form-control form-control-sm" value={this.state.title || ''} placeholder="我的仪表盘" data-id="title" onChange={this.handleChange} maxLength="40" />
          </div>
        </div>
        <div className="form-group row">
          <label className="col-sm-3 col-form-label text-sm-right"></label>
          <div className="col-sm-7">
            <label className="custom-control custom-control-sm custom-checkbox custom-control-inline mt-0 mb-0">
              <input className="custom-control-input" type="checkbox" checked={this.state.copy === true} data-id="copy" onChange={this.handleChange} />
              <span className="custom-control-label">复制当前仪表盘</span>
            </label>
          </div>
        </div>
        <div className="form-group row footer">
          <div className="col-sm-7 offset-sm-3">
            <button className="btn btn-primary" type="button" onClick={this.save}>确定</button>
          </div>
        </div>
      </div>
    </RbModal>)
  }
  save = () => {
    let _data = { title: this.state.title || '我的仪表盘' }
    _data.metadata = { entity: 'DashboardConfig' }
    if (this.state.copy === true) _data.__copy = gridstack_serialize

    $.post(rb.baseUrl + '/dashboard/dash-new', JSON.stringify(_data), (res) => {
      if (res.error_code === 0) {
        location.href = '?d=' + res.data.id
      } else rb.hberror(res.error_msg)
    })
  }
}

// 选择默认面板
class DashSelect extends React.Component {
  constructor(props) {
    super(props)
  }
  render() {
    return (
      <div className={'modal ' + (this.props.dlgClazz || 'dlg-dash-select')} ref="dlg" tabIndex="-1">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-header pb-0">
              <button className="close" type="button" onClick={() => this.hide()}><span className="zmdi zmdi-close" /></button>
            </div>
            <div className="modal-body">
              <div ref={s => this._scrollbar = s}>
                <ul className="list-unstyled">
                  {(this.props.dashList || []).map((item) => {
                    return <li key={'dash-' + item[0]}><a href={'?d=' + item[0]}>{item[1]}<i className="icon zmdi zmdi-arrow-right"></i></a></li>
                  })}
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }
  componentDidMount() {
    this.show()
  }
  hide() {
    $(this.refs['dlg']).modal('hide')
  }
  show() {
    $(this.refs['dlg']).modal({ show: true, keyboard: true })
  }
}

// 从已有图表中选择图表
// 添加的图表会在多个仪表盘共享（本身就是一个），修改时会同步修改
class ChartSelect extends RbModalHandler {
  constructor(props) {
    super(props)
    this.state = { chartList: [], appended: props.appended || [] }
  }
  render() {
    return (<RbModal ref={(c) => this._dlg = c} title="添加已有图表">
      <div className="chart-list">
        {this.state.chartList.map((item) => {
          return (<div key={'k-' + item[0]}>
            <span className="float-left chart-icon"><i className={item[2]}></i></span>
            <span className="float-left title">
              <strong>{item[1]}</strong>
              <p className="text-muted fs-12">{item[3]}</p>
            </span>
            <span className="float-right">
              {this.state.appended.contains(item[0])
                ? <a className='btn disabled' data-id={item[0]}>已添加</a>
                : <a className='btn' onClick={() => this.chartAppend(item)} >添加</a>}
            </span>
            <div className="clearfix"></div>
          </div>)
        })}
      </div>
    </RbModal>)
  }
  componentDidMount() {
    $.get(rb.baseUrl + '/dashboard/chart-list', (res) => {
      this.setState({ chartList: res.data })
    })
  }
  chartAppend(item) {
    add_widget({ chart: item[0], title: item[1], type: item[2], w: 4, h: 4 })
    let s = this.state.appended
    s.push(item[0])
    this.setState({ appended: s })
  }
}