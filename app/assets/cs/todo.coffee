$j = jQuery

$j ->

  class TodoList
    constructor: ->
      @resourceLink = $j("#todo-list").data("resourceLink")
      @todos = {}

      $j.get('/assets/templates/todoitem.mustache', (template) =>
        @todoTemplate = template)

      $j("#new-todo").keyup (ev) =>
        if ev.which == 13
          @createTodoItem({id: null, todo: ev.target.value, done: false})
          ev.target.value = ""

    renderTodos: () ->
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
      $j.ajax
        method: "DELETE"
        url: @resourceLink + "/" + idx
        dataType: "json"
        error: (jqXHR, textStatus, errorThrown) =>
          alert errorThrown
        success: (data, textStatus, jqXHR) =>
          @fetchAndRenderTodoList()

    updateTodoItem: (idx, data) ->
      $j.ajax
        method: "POST"
        url: @resourceLink + "/" + idx
        dataType: "json"
        data: JSON.stringify(data)
        contentType: "application/json"
        error: (jqXHR, textStatus, errorThrown) =>
          alert errorThrown
        success: (data, textStatus, jqXHR) =>
          @fetchAndRenderTodoList()

    createTodoItem: (data) ->
      $j.ajax
        method: "POST"
        url: @resourceLink
        dataType: "json"
        data: JSON.stringify(data)
        contentType: "application/json"
        error: (jqXHR, textStatus, errorThrown) =>
          alert errorThrown
        success: (data, textStatus, jqXHR) =>
          @fetchAndRenderTodoList()


  tl = new TodoList
  tl.fetchAndRenderTodoList()
