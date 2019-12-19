/* eslint-disable react/prop-types */
/* eslint-disable react/jsx-no-undef */

class RbFeeds extends React.Component {
  state = { ...this.props }

  render() {
    let s = $urlp('s', location.hash)
    if (s && s.length === 20) s = { field: 'feedsId', op: 'eq', value: s }
    else s = null

    return <React.Fragment>
      <FeedsPost ref={(c) => this._post = c} call={this.search} />
      <FeedsList ref={(c) => this._list = c} specFilter={s} />
    </React.Fragment>
  }
  search = (filter) => this._list.fetchFeeds(filter)
}

class GroupList extends React.Component {
  state = { ...this.props }

  render() {
    return (<ul className="list-unstyled">
      {!this.state.list && <li className="nodata">加载中</li>}
      {(this.state.list && this.state.list.length === 0) && <li className="nodata">暂无团队</li>}
      {(this.state.list || []).map((item) => {
        return <li key={'item-' + item.id} data-id={item.id} className={this.state.active === item.id ? 'active' : ''}>
          <a className="text-truncate" onClick={() => this._handleActive(item.id)}>{item.name}</a>
        </li>
      })}
    </ul>
    )
  }

  loadData() {
    $.get(`${rb.baseUrl}/feeds/group/group-list?all=true`, (res) => this.setState({ list: res.data || [] }))
  }

  _handleActive(id) {
    if (this.state.active === id) id = null
    this.setState({ active: id }, () => execFilter())
  }

  val() {
    return this.state.active
  }
}

class UserList extends GroupList {
  state = { ...this.props }
  loadData() {
    $.get(`${rb.baseUrl}/feeds/group/user-list`, (res) => {
      this.setState({ list: res.data || [] })
    })
  }
}

let rbFeeds
let rbGroupList
let rbUserList

// 构建搜索条件
const execFilter = function () {
  let group = rbGroupList.val()
  let user = rbUserList.val()
  let key = $('.J_search-key').val()
  let date1 = $('.J_date-begin').val()
  let date2 = $('.J_date-end').val()
  let type = ~~$('#collapseFeedsType li.active').data('type')

  let items = []
  if (group) items.push({ field: 'scope', op: 'EQ', value: group })
  if (user) items.push({ field: 'createdBy', op: 'EQ', value: user })
  if (key) items.push({ field: 'content', op: 'LK', value: key })
  if (date1) items.push({ field: 'createdOn', op: 'GE', value: date1 })
  if (date2) items.push({ field: 'createdOn', op: 'LE', value: date2 })
  if (type > 0) items.push({ field: 'type', op: 'EQ', value: type })

  rbFeeds.search({ entity: 'Feeds', equation: 'AND', items: items })
}

$(document).ready(function () {
  let gs = $urlp('gs', location.hash)
  if (gs) $('.search-input-gs, .J_search-key').val($decode(gs))

  renderRbcomp(<RbFeeds />, 'rb-feeds', function () { rbFeeds = this })
  renderRbcomp(<GroupList hasAction={true} />, $('#collapseGroup .dept-tree'), function () { rbGroupList = this })
  renderRbcomp(<UserList />, $('#collapseUser .dept-tree'), function () { rbUserList = this })

  let rbGroupListLoaded = false,
    rbUserListLoaded = false
  $('#headingGroup').click(() => {
    if (!rbGroupListLoaded) rbGroupList.loadData()
    rbGroupListLoaded = true
  })
  $('#headingUser').click(() => {
    if (!rbUserListLoaded) rbUserList.loadData()
    rbUserListLoaded = true
  })

  function __clear(el) {
    $setTimeout(() => {
      let $clear = $(el).next().find('a')
      if ($(el).val()) $clear.addClass('show')
      else $clear.removeClass('show')
    }, 50, 'Close-Show')
  }
  $('#collapseSearch .append>a').click(function () {
    let $i = $(this).parent().prev().val('')
    __clear($i)
    setTimeout(execFilter, 100)
  })

  $('.J_search-key').keydown(function (e) {
    __clear(this)
    if (e.keyCode === 13) execFilter()
  })
  let dpcfg = {
    navIcons: { rightIcon: 'zmdi zmdi-chevron-right', leftIcon: 'zmdi zmdi-chevron-left' },
    format: 'yyyy-mm-dd',
    minView: 2,
    startView: 'month',
    weekStart: 1,
    autoclose: true,
    language: 'zh',
    todayHighlight: true,
    showMeridian: false,
    endDate: new Date()
  }
  $('.J_date-begin, .J_date-end').datetimepicker(dpcfg).on('changeDate', function () {
    __clear(this)
    execFilter()
  })

  let lastType = 0
  $('#collapseFeedsType li>a').click(function () {
    $('#collapseFeedsType li').removeClass('active')
    let $li = $(this).parent()
    if (~~$li.data('type') === lastType) {
      lastType = 0
    } else {
      $li.addClass('active')
      lastType = ~~$li.data('type')
    }
    execFilter()
  })

  execFilter()

  // eslint-disable-next-line no-undef
  $showAnnouncement()
})

