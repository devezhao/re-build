/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */

var __NavTreeData = []

// ~ 目录树
class NavTree extends React.Component {
  state = { activeItem: 1, ...this.props }

  render() {
    return <div className="dept-tree p-0">
      <ul className="list-unstyled">
        {(this.state.list || []).map((item) => { return this._renderItem(item) })}
      </ul>
    </div>
  }

  _renderItem(item) {
    return <li key={`xd-${item.id}`} className={this.state.activeItem === item.id ? 'active' : ''}>
      <a data-id={item.id} onClick={() => this._clickItem(item)} href={`#!/Folder/${item.id}`}>{item.text}</a>
      {item.children && <ul className="list-unstyled">
        {item.children.map((item) => { return this._renderItem(item) })}
      </ul>}
    </li>
  }
  _clickItem(item) {
    this.setState({ activeItem: item.id }, () => {
      this.props.call && this.props.call(item)
    })
  }

  componentDidMount = () => this.loadData()
  loadData() {
    $.get(this.props.dataUrl, (res) => {
      let _list = res.data || []
      _list.unshift({ id: 1, text: '全部' })
      this.setState({ list: _list }, () => __NavTreeData = _list)
    })
  }
}

// ~ 文件列表
class FilesList extends React.Component {
  state = { ...this.props }

  render() {
    return <div className="file-list">
      {(this.state.files || []).map((item) => {
        return <div key={`file-${item.id}`} className="file-list-item">
          <span className="type"><i className="file-icon" data-type={item.fileType}></i></span>
          <span className="on">{item.uploadOn}</span>
          <span className="by">{item.uploadBy[1]}</span>
          <div className="detail">
            <a title="点击查看文件" onClick={() => previewFile(item.filePath)}>{$fileCutName(item.filePath)}</a>
            <div className="extra">{this.renderExtras(item)}</div>
          </div>
        </div>
      })}
      {(this.state.files && this.state.files.length === 0) && <div className="list-nodata pt-8 pb-8">
        <i className="zmdi zmdi-folder-outline"></i>
        <p>暂无相关文件</p>
      </div>}
    </div>
  }

  renderExtras(item) {
    return <span>{item.fileSize}</span>
  }

  componentDidMount = () => this.loadData()
  loadData(type) {
    $.get(this.buildDataUrl(type), (res) => {
      this.setState({ files: res.data || [] })
    })
  }

  buildDataUrl(type) {
    return `${rb.baseUrl}/files/list-file?entity=${type || 1}`
  }
}

// 文件预览
const previewFile = function (path) {
  RbPreview.create(path)
}