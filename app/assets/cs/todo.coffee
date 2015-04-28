$j = jQuery

$j ->

  class TodoList
    constructor: ->
      @resourceLink = $j("#todo-list").data("resourceLink")
      @wsLink = $j("#todo-list").data("realtimeLink")
      @todos = {}

      $j.get('/assets/templates/todoitem.mustache', (template) =>
        @todoTemplate = template)

      $j("#new-todo").keyup (ev) =>
        if ev.which == 13
          @createTodoItem({id: null, todo: ev.target.value, done: false})
          ev.target.value = ""

      @ws = new WebSocket(@wsLink)
      @ws.onmessage = @wsUpdate

    wsUpdate: (ev) =>
      msg = JSON.parse(ev.data)

      switch msg.action
        when "add" then @addTodo msg
        when "update" then @updateTodo msg
        when "delete" then @removeTodo msg

    addTodo: (msg) ->
      @todos.todos.push({id: msg.todoid, todo: msg.todo, done: msg.done})
      @renderTodos()

    updateTodo: (msg) ->
      todoItem = _.find(@todos.todos, (t) -> t.id == msg.todoid)
      todoItem.todo = msg.todo
      todoItem.done = msg.done
      @renderTodos()

    removeTodo: (msg) ->
      @todos.todos = _.filter(@todos.todos, (t) -> t.id != msg.todoid)
      @renderTodos()

    renderTodos: () ->
      @todos.todos = _.sortBy(@todos.todos, (el) -> -el.id)
      $j("#todo-list").html(Mustache.render(@todoTemplate, @todos))

      $j(".destroy").click (ev) =>
        todoId = $j(ev.target).data("id")
        @removeTodoItem(todoId)

      $j(".toggle").click (ev) =>
        todoId = $j(ev.target).data("id")
        todoItemData = _.find(@todos.todos, (t) -> t.id == todoId)
        todoItemData.done = ! todoItemData.done
        @updateTodoItem(todoId, todoItemData)

    fetchAndRenderTodoList: () ->
      $j.ajax
        url: @resourceLink
        dataType: "json"
        error: (jqXHR, textStatus, errorThrown) =>
          alert errorThrown
        success: (data, textStatus, jqXHR) =>
          @todos = data
          @renderTodos()

    removeTodoItem: (idx) ->
      @ws.send(JSON.stringify({action: "delete", todoid: idx, todo: null, done: null}))

    updateTodoItem: (idx, data) ->
      @ws.send(JSON.stringify({action: "update", todoid: idx, todo: data.todo, done: data.done}))

    createTodoItem: (data) ->
      @ws.send(JSON.stringify({action: "add", todoid: null, todo: data.todo, done: data.done}))


  tl = new TodoList
  tl.fetchAndRenderTodoList()
