<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<script>
$(document).ready(function() {
	document.getElementById('counter').value="${counter_val}";
	document.getElementById('but').style.display = "block";
});
document.getElementById('bord').style.display = "block";
</script>
<input type="hidden" name="connection_id" id="connection_id"
	class="form-control" value="${conn_val.connection_id}">
<c:forEach items="${arrddb}" var="arrddb" varStatus="theCount">
	<div class="form-group row" id="schm_div${theCount.count}">
		<div class="col-sm-10">
			<label>Schema Name *</label> <input list="schemas${theCount.count}"
				name="schema_name${theCount.count}" value="${arrddb.schema_name}"
				id="schema_name${theCount.count}" class="form-control"
				onchange="getsch(this.id,this.value)">
			<datalist id="schemas${theCount.count}">
			<c:forEach items="${schema_name}" var="schema_name">
				<option value="${schema_name}">
			</c:forEach>
			</datalist>
		</div>
		<div class="col-sm-2">
			<button id="del${theCount.count}" type="button"
				class="btn btn-rounded btn-gradient-danger mr-2"
				onclick="delblock(this.id)">X</button>
		</div>
	</div>
	<div id="datdiv${theCount.count}">
		<div id="ind_load${theCount.count}">
			<div id="tbl_fld${theCount.count}">
				<div class="form-group row">
					<div class="col-sm-6">
						<label>Select Table *</label> <input
							list="tables${theCount.count}" name="table_name${theCount.count}" 
							id="table_name${theCount.count}" value="${arrddb.schema_name}.${arrddb.table_name}" class="form-control"
							onchange="getcols(this.id,this.value)">
						<datalist id="tables${theCount.count}">
							<option value="${arrddb.schema_name}.${arrddb.table_name}">
						</datalist>
					</div>
					<div class="col-sm-6">
						<label>Load Type *</label> <select class="form-control"
							id="fetch_type${theCount.count}"
							name="fetch_type${theCount.count}"
							onchange="incr(this.id,this.value)">
							<c:choose>
								<c:when test="${ext_type=='Real'}">
									<option value="full" selected>Full Load</option>
								</c:when>
								<c:otherwise>
									<c:choose>
										<c:when test="${arrddb.fetch_type=='full'}">
											<option value="full" selected>Full Load</option>
										</c:when>
										<c:when test="${arrddb.fetch_type=='incr'}">
											<option value="incr" selected>Incremental Load</option>
										</c:when>
										<c:otherwise>
											<option value="full">Full Load</option>
											<option value="incr">Incremental Load</option>
										</c:otherwise>
									</c:choose>
								</c:otherwise>
							</c:choose>
						</select>
					</div>
				</div>
				<div id="fldd${theCount.count}">
					<c:choose>
						<c:when test="${arrddb.fetch_type=='incr'}">
							<div class="form-group" id="incc${theCount.count}"
								style="display: block;">
								<label>Select Incremental Column *</label> <select
									class="form-control" id="incr_col${theCount.count}"
									name="incr_col${theCount.count}">
									<c:forTokens items="${arrddb.cols}" delims="," var="mySplitx">
										<c:choose>
											<c:when test="${arrddb.incr_column==mySplitx}">
												<option value="${mySplitx}" selected>${mySplitx}</option>
											</c:when>
											<c:otherwise>
												<option value="${mySplitx}">${mySplitx}</option>
											</c:otherwise>
										</c:choose>
									</c:forTokens>
								</select>
							</div>
						</c:when>
						<c:otherwise>
							<div class="form-group" id="incc${theCount.count}"
								style="display: none;">
								<label>Select Incremental Column *</label> <select
									class="form-control" id="incr_col${theCount.count}"
									name="incr_col${theCount.count}">
									<c:forTokens items="${arrddb.cols}" delims="," var="mySplitx">
										<c:choose>
											<c:when test="${arrddb.incr_column==mySplitx}">
												<option value="${mySplitx}" selected>${mySplitx}</option>
											</c:when>
											<c:otherwise>
												<option value="${mySplitx}">${mySplitx}</option>
											</c:otherwise>
										</c:choose>
									</c:forTokens>
								</select>
							</div>
						</c:otherwise>
					</c:choose>

					<div>
						<div
							style="float: left; width: 33%; height: 25px; font-weight: bold; text-align: center;">Available
							Fields</div>
						<div
							style="float: left; width: 33%; height: 25px; font-weight: bold; text-align: center;">Selected
							Fields</div>
						<div
							style="float: left; width: 33%; height: 25px; font-weight: bold; text-align: center;">Tokenized
							Fields</div>
					</div>
					<div>
						<div
							style="float: left; width: 33%; height: 300px; overflow-y: scroll;"
							id="avl${theCount.count}" ondrop="drop(event,this)"
							ondragover="allowDrop(event)">
							<button id="but${theCount.count}" name="but${theCount.count}"
								value="*" class="btn btn-dark" draggable="true"
								ondragstart="drag(event)"
								style="width: 90%; margin: 5px; padding: 10px 0px;"
								onclick="return false;">Select All</button>
							<c:forTokens items="${arrddb.unsel_cols}" delims="," var="mySplit">
								<button id="${mySplit}${theCount.count}" name="${mySplit}${theCount.count}" value="${mySplit}"
									class="btn btn-dark" draggable="true" ondragstart="drag(event)"
									style="width: 90%; margin: 5px; padding: 10px 0px;"
									onclick="return false;">${mySplit}</button>
							</c:forTokens>
						</div>
						<div
							style="float: left; width: 33%; height: 300px; overflow-y: scroll;"
							id="sel${theCount.count}" ondrop="drop(event,this)"
							ondragover="allowDrop(event)">
							<c:forTokens items="${arrddb.column_name}" delims="," var="mySplit">
								<button id="${mySplit}${theCount.count}" name="${mySplit}${theCount.count}" value="${mySplit}"
									class="btn btn-dark" draggable="true" ondragstart="drag(event)"
									style="width: 90%; margin: 5px; padding: 10px 0px;"
									onclick="return false;">${mySplit}</button>
							</c:forTokens>
							</div>
						<div
							style="float: left; width: 33%; height: 300px; overflow-y: scroll;"
							id="tok${theCount.count}" ondrop="drop(event,this)"
							ondragover="allowDrop(event)"></div>
					</div>

					<input type="hidden" name="col_name${theCount.count}"
						id="col_name${theCount.count}" class="form-control"> <input
						type="hidden" name="columns_name${theCount.count}"
						id="columns_name${theCount.count}" class="form-control"> <input
						type="hidden" name="token${theCount.count}"
						id="token${theCount.count}" class="form-control">
					<div class="form-group">
						<label>Where Condition *</label>
						<textarea class="form-control" id="where_clause${theCount.count}"
							name="where_clause${theCount.count}" style="width: 100%;"
							placeholder="column1='filter1' and (column2>'filter2' or column3<'filter3')">${arrddb.where_clause}</textarea>
					</div>
				</div>
			</div>
		</div>
	</div>
</c:forEach>