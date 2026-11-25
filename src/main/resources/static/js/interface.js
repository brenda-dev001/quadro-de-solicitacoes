function atualizarNavegacao() {
    document.querySelectorAll('[data-nav]').forEach(link => {
        const caminho = link.dataset.nav;
        const ativo = caminho === '/' ? location.pathname === '/' : location.pathname.startsWith(caminho);
        link.classList.toggle('active', ativo);
        if (ativo) link.setAttribute('aria-current', 'page');
        else link.removeAttribute('aria-current');
    });
}

function prepararInterface(container = document) {
    atualizarNavegacao();
    container.querySelectorAll('input[required], textarea[required]').forEach(campo => {
        if (campo.dataset.validacaoPronta) return;
        campo.dataset.validacaoPronta = 'true';
        campo.addEventListener('input', () => {
            campo.setCustomValidity(campo.value.trim() ? '' : 'Preencha este campo. Espaços em branco não são válidos.');
        });
    });
    container.querySelectorAll('[maxlength]').forEach(campo => {
        if (campo.closest('.filter-body')) return;
        if (campo.dataset.contadorPronto) return;
        campo.dataset.contadorPronto = 'true';
        const contador = document.createElement('p');
        contador.className = 'field-hint';
        const atualizar = () => contador.textContent = `${campo.value.length} / ${campo.maxLength} caracteres`;
        campo.closest('.relative')?.after(contador);
        campo.addEventListener('input', atualizar);
        atualizar();
    });
    container.querySelectorAll('.searchable-select-container').forEach(seletor => {
        if (seletor.dataset.tecladoPronto) return;
        seletor.dataset.tecladoPronto = 'true';
        const trigger = seletor.querySelector('.custom-select-trigger');
        const dropdown = seletor.querySelector('.custom-select-dropdown');
        const busca = dropdown.querySelector('input');
        const lista = seletor.querySelector('.options-list');
        lista.setAttribute('role', 'listbox');
        const prepararOpcoes = () => lista.querySelectorAll('li[data-value]').forEach(item => {
            item.tabIndex = -1;
            item.setAttribute('role', 'option');
        });
        new MutationObserver(prepararOpcoes).observe(lista, { childList: true });
        new MutationObserver(() => trigger.setAttribute('aria-expanded', String(!dropdown.classList.contains('hidden'))))
            .observe(dropdown, { attributes: true, attributeFilter: ['class'] });
        trigger.addEventListener('keydown', event => {
            if (event.target !== trigger) return;
            if (['Enter', ' ', 'ArrowDown'].includes(event.key)) {
                event.preventDefault();
                if (dropdown.classList.contains('hidden')) trigger.click();
                else lista.querySelector('li[data-value]')?.focus();
            }
        });
        dropdown.addEventListener('keydown', event => {
            const opcoes = [...lista.querySelectorAll('li[data-value]')];
            const posicao = opcoes.indexOf(document.activeElement);
            if (event.key === 'Escape') {
                event.preventDefault();
                dropdown.classList.add('hidden');
                trigger.focus();
            } else if (event.key === 'ArrowDown' && opcoes.length) {
                event.preventDefault();
                opcoes[(posicao + 1) % opcoes.length].focus();
            } else if (event.key === 'ArrowUp' && opcoes.length) {
                event.preventDefault();
                opcoes[(posicao + opcoes.length - 1) % opcoes.length].focus();
            } else if (event.key === 'Enter') {
                event.preventDefault();
                (opcoes[posicao] || opcoes[0])?.click();
                trigger.focus();
            }
        });
        lista.addEventListener('click', event => {
            if (!event.target.closest('li[data-value]')) return;
            trigger.removeAttribute('aria-invalid');
            seletor.querySelector('.client-error')?.remove();
            trigger.focus();
        });
        const botaoBusca = seletor.querySelector('[data-search-toggle]');
        const painelBusca = seletor.querySelector('[data-search-panel]');
        botaoBusca.addEventListener('click', () => {
            painelBusca.hidden = false;
            botaoBusca.hidden = true;
            busca.focus();
        });
        seletor.querySelector('[data-search-close]').addEventListener('click', () => {
            painelBusca.hidden = true;
            botaoBusca.hidden = false;
            busca.value = '';
            busca.dispatchEvent(new Event('carregar-opcoes', { bubbles: true }));
            botaoBusca.focus();
        });
        prepararOpcoes();
    });
    const erro = container.querySelector('.alert-error');
    if (erro) erro.focus({ preventScroll: false });
    requestAnimationFrame(atualizarNavegacao);
}

document.addEventListener('submit', event => {
    const form = event.target;
    if (!form.matches('form[data-validate]')) return;
    let primeiroErro = null;
    form.querySelectorAll('.hidden-select-input[data-required=true]').forEach(campo => {
        const seletor = campo.closest('.searchable-select-container');
        seletor.querySelector('.client-error')?.remove();
        const trigger = seletor.querySelector('.custom-select-trigger');
        if (!/^[1-9]\d*$/.test(campo.value)) {
            const mensagem = document.createElement('p');
            mensagem.className = 'client-error';
            mensagem.setAttribute('role', 'alert');
            mensagem.textContent = 'Selecione uma opção da lista.';
            seletor.append(mensagem);
            trigger.setAttribute('aria-invalid', 'true');
            primeiroErro ||= trigger;
        }
    });
    if (primeiroErro) {
        event.preventDefault();
        event.stopImmediatePropagation();
        primeiroErro.focus();
    }
}, true);

document.addEventListener('DOMContentLoaded', () => {
    prepararInterface();
    if (typeof htmx !== 'undefined') htmx.onLoad(container => prepararInterface(container));
});
window.addEventListener('popstate', () => requestAnimationFrame(atualizarNavegacao));
