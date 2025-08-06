<div id="${name!}_container" style="display:none;margin:5px 0;">
    <input id="${name!}" name="${name!}" type="hidden" value="${value!?html}" />
    <#if element.properties.showLabel! == "true" >
        <label><strong>${label!?html} :</strong></label>&nbsp;&nbsp;
    </#if>
    <#list options?keys as key>
        <a ref="${key?html}" href="${key?html}" class="<#if value! == key >active</#if>"><span><#if value! == key ><strong></#if>${options[key]!?html}<#if value! == key ></strong></#if></span></a>&nbsp;&nbsp;
    </#list>

    <script type="text/javascript">
        $(document).ready(function(){
            <#if element.properties.displayFull! == "true" >
                $('#${name!}_container').insertBefore($('#${name!}_container').closest(".filters"));
            </#if>
            $('#${name!}_container').show();
            $('#${name!}_container a').click(function(){
                var value = $(this).attr("ref");
                $(this).parent().find("input").val(value);
                $(this).closest("form").submit();
                return false;
            });
        });
    </script>
</div>