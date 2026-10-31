$(document).ready(function () {
    const maxOptions = 10;

    // HTML blocks to add dynamics to
    let wrapper = $("#addOptionWrapper");
    let addOptionButton = $("#addOptionButton");

    let x = 0;
    $(addOptionButton).click(function (e) {
        e.preventDefault();
        if (x < maxOptions) {
            x++;

            $(wrapper).append('<div class="row">'
                + '<div id="optionsRow' + x + '" class="form-group col-md-4">'
                + '<div class="form-group col-md-3">'
                + '<label htmlFor="options' + x + '" className="col-form-label">Inhalt</label>'
                + '<input id="options" th:field="*{options}" type="text" className="form-control" placeholder="Deine Wahloption"/>'
                + '<span th:if="${#fields.hasErrors(\'options\')}" th:errors="*{options}" className="text-danger"></span>'
                + '</div>'
                + '<a href="#" class="removeOption col-md-1"> Eintrag löschen </a>'
                + '</div>');
        }
    });

    $(wrapper).on("click", ".removeOption", function (e) {
        e.preventDefault();
        $(this).parent('div').remove();
        x--;
    })
});
