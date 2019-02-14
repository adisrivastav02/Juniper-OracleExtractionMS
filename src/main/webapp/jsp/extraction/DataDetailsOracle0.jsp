<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script>
	document.getElementById('bord').style.display = "block";
	function getsch(id, val) {
		var in1 = id.slice(-1);
		var in2 = id.slice(-2, -1);
		if (in2 === "e")
			;
		else {
			in1 = id.slice(-2);
		}
		var id = in1;
		var schema_name = val;
		var src_val = document.getElementById("src_val").value;
		var src_sys_id = document.getElementById("feed_id").value;
		$
				.post(
						'${pageContext.request.contextPath}/extraction/DataDetailsOracle1',
						{
							id : id,
							src_sys_id : src_sys_id,
							src_val : src_val,
							schema_name : schema_name
						}, function(data) {
							$('#datdiv' + id).html(data)
						});
	}
</script>
<div class="form-group row" id="schm_div1">
	<div class="col-sm-10">
		<label>Schema Name *</label> <input list="schemas1"
			name="schema_name1" id="schema_name1" class="form-control"
			onchange="getsch(this.id,this.value)">
		<datalist id="schemas1">
			<c:forEach items="${schema_name}" var="schema_name">
				<option value="${schema_name}">
			</c:forEach>
		</datalist>
	</div>
	<div class="col-sm-2">
		<button id="del1" type="button"
			class="btn btn-rounded btn-gradient-danger mr-2"
			onclick="delblock(this.id)">X</button>
	</div>
	<!-- <select name="schema_name1"
		id="schema_name1" class="form-control" onchange="getsch(this.id,this.value)">
		<option value="" selected disabled>Schema Name ...</option>
		<c:forEach items="${schema_name}" var="schema_name">
			<option value="${schema_name}">${schema_name}</option>
		</c:forEach>
	</select>-->
</div>
<div id="datdiv1"></div>