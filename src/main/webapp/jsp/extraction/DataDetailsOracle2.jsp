<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script>
var i = document.getElementById('counter').value;
function dup_div() {
	i++;
		var clone=$("#schm_div1").clone().attr('id', 'schm_div'+i).insertAfter("#schm_div1");
		/*clone.find("select").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i },
			    'name': function(_, name) { return name.slice(0,-1) + i },
			    'value': ''               
			});
	  	}).end().appendTo("#datadiv");*/
	  	clone.find("input").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i },
			    'name': function(_, name) { return name.slice(0,-1) + i },
			    'list': function(_, list) { return list.slice(0,-1) + i }             
			});
	  	}).end().appendTo("#datadiv");
	  	clone.find("datalist").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i }         
			});
	  	}).end().appendTo("#datadiv");
		var clone1=$("#datdiv1").clone().attr('id', 'datdiv'+i).insertAfter("#datdiv1");
		/* clone1.find("input").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i },
			    'name': function(_, name) { return name.slice(0,-1) + i },
			    'list': function(_, list) { return list.slice(0,-1) + i }              
			});
	  	}).end().appendTo("#datadiv");
		clone1.find("datalist").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i }         
			});
	  	}).end().appendTo("#datadiv"); */
		clone1.find("select").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i },
			    'name': function(_, name) { return name.slice(0,-1) + i },
			    'value': ''              
		    });
		}).end().appendTo("#datadiv");
		clone1.find("textarea").each(function() {
			$(this).attr({
				'id': function(_, id) { return id.slice(0,-1) + i },
			    'name': function(_, name) { return name.slice(0,-1) + i },
			    'value': ''              
		    });
		}).end().appendTo("#datadiv");
		clone1.find("div").each(function() {
			$(this).attr({
				'id': function(_, id) { if(id) return id.slice(0,-1) + i },
			    'name': function(_, name) { if(name) return name.slice(0,-1) + i }
		    });
		}).end().appendTo("#datadiv");
		$('#table_name'+i).find('option').remove().end().append('<option value="">Table ...</option>').val('');
		$('#col_name'+i).find('option').remove().end().append('<option value="">Columns ...</option>').val('');
		document.getElementById('counter').value=i;
}
function allowDrop(ev) {
	ev.preventDefault();
}
function drag(ev) {
	ev.dataTransfer.setData("text", ev.target.id);
}
function drop(ev,el) {
	ev.preventDefault();
	var data = ev.dataTransfer.getData("text");
	el.appendChild(document.getElementById(data));
}
</script>
<div class="form-group" id="incc${id}" style="display: none;">
	<label>Select Incremental Column *</label> <select class="form-control"
		id="incr_col${id}" name="incr_col${id}">
		<option value="" selected disabled>Incremental Column...</option>
		<c:forEach var="fields" items="${fields}">
			<option value="${fields}">${fields}</option>
		</c:forEach>
	</select>
</div>

<div>
<div style="float:left;width:33%;height:25px;font-weight:bold;text-align:center;">Available Fields</div>
<div style="float:left;width:33%;height:25px;font-weight:bold;text-align:center;">Selected Fields</div>
<div style="float:left;width:33%;height:25px;font-weight:bold;text-align:center;">Tokenized Fields</div>
</div>	
<div>
<div style="float:left;width:33%;height:300px;overflow-y:scroll;" id="avl${id}" ondrop="drop(event,this)" ondragover="allowDrop(event)">
<button id="but${id}" name="but${id}" value="*" class="btn btn-dark" draggable="true" ondragstart="drag(event)" style="width:90%;margin:5px;padding:10px 0px;" onclick="return false;">Select All</button>
<c:forEach var="fields" items="${fields}">
<button id="${fields}" name="${fields}" value="${fields}" class="btn btn-dark" draggable="true" ondragstart="drag(event)" style="width:90%;margin:5px;padding:10px 0px;" onclick="return false;">${fields}</button>
</c:forEach>
</div>
<div style="float:left;width:33%;height:300px;overflow-y:scroll;" id="sel${id}" ondrop="drop(event,this)" ondragover="allowDrop(event)"></div>
<div style="float:left;width:33%;height:300px;overflow-y:scroll;" id="tok${id}" ondrop="drop(event,this)" ondragover="allowDrop(event)"></div>
</div>

<input type="hidden" name="col_name${id}" id="col_name${id}" class="form-control">
<input type="hidden" name="columns_name${id}" id="columns_name${id}" class="form-control">
<input type="hidden" name="token${id}" id="token${id}" class="form-control">
<div class="form-group">
	<label>Where Condition *</label>
	<textarea class="form-control" id="where_clause${id}" name="where_clause${id}"
		style="width: 100%;"
		placeholder="column1='filter1' and (column2>'filter2' or column3<'filter3')"></textarea>
</div>

<script>
	/*var cnt = document.getElementById('counter').value;
	var	select = document.getElementById('col_name' + cnt);
	multi(select, {});*/
	document.getElementById('but').style.display="block";
</script>