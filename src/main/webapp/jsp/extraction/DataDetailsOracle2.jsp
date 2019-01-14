<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="form-group" id="incc${id}" style="display: none;">
	<label>Select Incremental Column *</label> <select class="form-control"
		id="incr_col${id}" name="incr_col${id}">
		<option value="" selected disabled>Incremental Column...</option>
		<c:forEach var="fields" items="${fields}">
			<option value="${fields}">${fields}</option>
		</c:forEach>
	</select>
</div>
<div class="form-group">
	<label>Select Columns *</label> <select class="form-control"
		id="col_name${id}" name="col_name${id}" multiple="multiple">
		<option value="*">Select All</option>
		<c:forEach var="fields" items="${fields}">
			<option value="${fields}">${fields}</option>
		</c:forEach>
	</select>
</div>
<input type="hidden" name="columns_name${id}" id="columns_name${id}" class="form-control">

<script> 
	var cnt = document.getElementById('counter').value;
	var select = document.getElementById('col_name'+cnt);
	multi(select, {});
</script>