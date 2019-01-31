<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<input type="hidden" name="connection_id" id="connection_id"
	class="form-control" value="${conn_val.connection_id}">
<input type="hidden" name="counter" id="counter" class="form-control"
	value="1">

<div id="ind_load"
	style="border: 1px groove #DCDCDC; border-radius: 10px; padding: 10px; overflow: hidden;">
	<div id="tbl_fld">
		<div class="form-group row">
			<div class="col-sm-6">
				<label>Select Table *</label> <select class="form-control"
					id="table_name1" name="table_name1"
					onchange="getcols(this.id,this.value)">
					<option value="" selected disabled>Table...</option>
					<c:forEach items="${tables}" var="tables">
						<option value="${schema_name}.${tables}">${tables}</option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-6">
				<label>Load Type *</label> <select class="form-control"
					id="fetch_type1" name="fetch_type1"
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
		<div id="fldd1"></div>
		<div class="form-group">
			<label>Where Condition *</label>
			<textarea class="form-control" id="where_clause1"
				name="where_clause1" style="width: 100%;"
				placeholder="column1='filter1' and (column2>'filter2' or column3<'filter3')"></textarea>
		</div>
	</div>
	<div class="form-group" style="float: right; margin: 5px;">
		<button id="add" type="button"
			class="btn btn-rounded btn-gradient-info mr-2"
			onclick="return dup_div();">+</button>
	</div>
	<button onclick="return jsonconstruct();"
		class="btn btn-rounded btn-gradient-info mr-2">Save</button>
</div>