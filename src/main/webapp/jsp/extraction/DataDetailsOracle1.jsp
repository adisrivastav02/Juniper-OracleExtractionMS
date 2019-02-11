<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script>
function getcols(id, val) {
	var in1 = id.slice(-1);
	var in2 = id.slice(-2, -1);
	if (in2 === "e")
		;
	else {
		in1 = id.slice(-2);
	}
	var id = in1;
	var table_name = val;
	var src_val = document.getElementById("src_val").value;
	var connection_id = document.getElementById("connection_id").value;
	var schema_name = document.getElementById("schema_name"+id).value;
	$.post('${pageContext.request.contextPath}/extraction/DataDetailsOracle2',
	{
		id : id,
		src_val : src_val,
		table_name : table_name,
		connection_id : connection_id,
		schema_name : schema_name
	}, function(data) {
		$("#fldd"+id).html(data);
	});
}
function incr(id, val) {
	var in1 = id.slice(-1);
	var in2 = id.slice(-2, -1);
	if (in2 === "e")
		;
	else {
		in1 = id.slice(-2);
	}
	var in3 = 'incc' + in1;
	if (val == "incr") {
		document.getElementById(in3).style.display = "block";
	} else if (val == "full") {
		document.getElementById(in3).style.display = "none";
	}
}
</script>

<input type="hidden" name="connection_id" id="connection_id"
	class="form-control" value="${conn_val.connection_id}">

<div id="ind_load${id}">
	<div id="tbl_fld${id}">
		<div class="form-group row">
			<div class="col-sm-6">
				<label>Select Table *</label> 
				
				<input list="tables${id}" name="table_name${id}" id="table_name${id}" class="form-control" onchange="getcols(this.id,this.value)">
			  	<datalist id="tables${id}">
			    	<c:forEach items="${tables}" var="tables">
						<option value="${schema_name}.${tables}">
					</c:forEach>
			  	</datalist>
				
				<!-- <select class="form-control"
					id="table_name${id}" name="table_name${id}"
					onchange="getcols(this.id,this.value)">
					<option value="" selected disabled>Table...</option>
					<c:forEach items="${tables}" var="tables">
						<option value="${schema_name}.${tables}">${tables}</option>
					</c:forEach>
				</select>-->
			</div>
			<div class="col-sm-6">
				<label>Load Type *</label> <select class="form-control"
					id="fetch_type${id}" name="fetch_type${id}"
					onchange="incr(this.id,this.value)">
					<c:choose>
						<c:when test="${ext_type=='Real'}">
							<option value="full" selected>Full Load</option>
						</c:when>
						<c:otherwise>
							<option value="" selected disabled>Load Type ...</option>
							<option value="full">Full Load</option>
							<option value="incr">Incremental Load</option>
						</c:otherwise>
					</c:choose>
				</select>
			</div>
		</div>
		<div id="fldd${id}"></div>
	</div>
</div>